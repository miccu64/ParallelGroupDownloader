package server;

import common.StatusEnum;
import common.exceptions.DownloadException;
import common.services.ChecksumService;
import common.utils.FilePartUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DownloaderTests {
    private final static ConcurrentLinkedQueue<Path> filesToDelete = new ConcurrentLinkedQueue<>();
    private final static String testDirectory = String.valueOf(Paths.get(CommonUtils.testDirectory, "DownloaderTests"));
    private final static String fileDownloaderDirectory = String.valueOf(Paths.get(testDirectory, "FileDownloader"));

    @BeforeAll
    public static void beforeAll() throws IOException {
        CommonUtils.beforeAll(testDirectory);
        CommonUtils.beforeAll(fileDownloaderDirectory);
    }

    @AfterAll
    public static void afterAll() {
        CommonUtils.afterAll(testDirectory, new ArrayList<>(filesToDelete));
    }

    @Test
    public void shouldOriginalAndDownloadedFilesMatchChecksums() throws IOException, DownloadException {
        // Arrange
        String fileName = "shouldOriginalAndDownloadedFilesMatchChecksums";
        Path filePathToDownload = CommonUtils.generateFile(fileName, testDirectory, 5);
        filesToDelete.add(filePathToDownload);

        URL url = filePathToDownload.toUri().toURL();

        // Act
        FileDownloader fileDownloader = createFileDownloader(url.toString(), 2);
        StatusEnum result = fileDownloader.call();
        filesToDelete.addAll(fileDownloader.getProcessedFiles());

        Assertions.assertEquals(StatusEnum.Success, result);
        Assertions.assertDoesNotThrow(() -> FilePartUtils.joinAndRemoveFileParts(fileDownloader.getProcessedFiles()));

        Path downloadedFile = Paths.get(fileDownloaderDirectory, fileName);
        filesToDelete.add(downloadedFile);

        ChecksumService checksumService = new ChecksumService();
        checksumService.addFileToProcess(downloadedFile);
        checksumService.addFileToProcess(filePathToDownload);
        String downloadedChecksum = checksumService.getChecksums().get(0);
        String originalChecksum = checksumService.getChecksums().get(1);

        // Assert
        Assertions.assertEquals(originalChecksum, downloadedChecksum);
    }

    @Test
    public void shouldDivideDownloadedFileWithExactPartSize() throws IOException, DownloadException {
        // Arrange
        String fileName = "shouldDivideDownloadedFileWithExactPartSize";
        Path filePathToDownload = generateFile(fileName, 3);

        URL url = filePathToDownload.toUri().toURL();
        int blockSizeInMB = 1;

        // Act
        FileDownloader fileDownloader = createFileDownloader(url.toString(), blockSizeInMB);
        StatusEnum result = fileDownloader.call();
        filesToDelete.addAll(fileDownloader.getProcessedFiles());

        Assertions.assertEquals(StatusEnum.Success, result);

        Path firstPart = fileDownloader.getProcessedFiles().get(0);
        long sizeInBytes = Files.size(firstPart);
        int fileSizeInMB = FilePartUtils.bytesToMegabytes(sizeInBytes);

        // Assert
        Assertions.assertEquals(blockSizeInMB, fileSizeInMB);
    }

    @Test
    public void shouldPartSummarySizeEqualJoinedSize() throws IOException, DownloadException {
        // Arrange
        String fileName = "shouldPartSummarySizeEqualJoinedSize";
        Path filePathToDownload = generateFile(fileName, 5);

        URL url = filePathToDownload.toUri().toURL();

        // Act
        FileDownloader fileDownloader = createFileDownloader(url.toString(), 2);
        StatusEnum result = fileDownloader.call();
        List<Path> processedFiles = fileDownloader.getProcessedFiles();
        filesToDelete.addAll(processedFiles);

        Assertions.assertEquals(StatusEnum.Success, result);

        long sizeInBytes = 0;
        for (Path filePart : processedFiles) {
            sizeInBytes += Files.size(filePart);
        }

        Path finalFilePath = FilePartUtils.joinAndRemoveFileParts(processedFiles);
        filesToDelete.add(finalFilePath);
        long sizeInBytesAfterJoin = Files.size(finalFilePath);

        // Assert
        Assertions.assertEquals(sizeInBytes, sizeInBytesAfterJoin);
    }

    @Test
    public void shouldFileDownloaderGetProperLocalFileSize() throws IOException, DownloadException {
        // Arrange
        String fileName = "shouldFileDownloaderGetProperLocalFileSize";
        Path filePathToDownload = generateFile(fileName, 1);

        int expectedSize = FilePartUtils.bytesToMegabytes(Files.size(filePathToDownload));
        URL url = filePathToDownload.toUri().toURL();

        // Act
        FileDownloader fileDownloader = createFileDownloader(url.toString(), 1);
        long fileDownloaderSize = fileDownloader.getFileSizeInMB();

        // Assert
        Assertions.assertEquals(expectedSize, fileDownloaderSize);
    }

    @Test
    public void shouldReturnProperFileNameFromLocalFile() throws IOException, DownloadException {
        // Arrange
        String expectedFileName = "shouldReturnProperFileNameFromLocalFile";
        Path filePathToDownload = generateFile(expectedFileName, 1);

        URL url = filePathToDownload.toUri().toURL();

        // Act
        FileDownloader fileDownloader = createFileDownloader(url.toString(), 1);
        String fileName = fileDownloader.getFileName();

        // Assert
        Assertions.assertEquals(expectedFileName, fileName);
    }

    @Test
    public void shouldReturnProperFileNameFromUrl() throws DownloadException {
        // Arrange
        String expectedFileName = "file.txt";
        String url = "http://not-existing-url-32176573546.pl/" + expectedFileName;

        // Act
        FileDownloader fileDownloader = createFileDownloader(url, 1);
        String fileName = fileDownloader.getFileName();

        // Assert
        Assertions.assertEquals(expectedFileName, fileName);
    }

    @Test
    public void shouldNotReturnSizeWhenCannotGetIt() throws DownloadException {
        // Arrange
        String url = "http://not-existing-url-32176573546.pl/file.txt";

        // Act
        FileDownloader fileDownloader = createFileDownloader(url, 1);
        long size = fileDownloader.getFileSizeInMB();

        // Assert
        Assertions.assertEquals(0, size);
    }

    @Test
    public void shouldThrowWhenFileNotExists() throws IOException, DownloadException {
        // Arrange
        Path notExistingFile = Paths.get("shouldThrowWhenFileNotExists.file").toAbsolutePath();
        File f = notExistingFile.toFile();
        Assertions.assertFalse(f.exists());

        String notExistingFileUrl = notExistingFile.toUri().toURL().toString();
        FileDownloader fileDownloader = createFileDownloader(notExistingFileUrl, 1);

        // Act
        StatusEnum result = fileDownloader.call();

        // Assert
        Assertions.assertEquals(StatusEnum.Error, result);
    }

    @Test
    public void shouldThrowWhenIsMalformedLocalFileUrl() {
        // Arrange
        String fileName = "shouldThrowWhenIsMalformedLocalFileUrl";
        String malformedUrl = Paths.get(fileName + ".file").toString();

        // Act
        Assertions.assertThrowsExactly(DownloadException.class, () -> createFileDownloader(malformedUrl, 1));
    }

    @Test
    public void shouldReturnErrorWhenUrlDoesNotExist() throws DownloadException {
        // Arrange
        String malformedUrl = "http://not-existing-url-32176573546.pl/file.txt";

        // Act
        StatusEnum status = new FileDownloader(malformedUrl, 1, null).call();

        // Assert
        Assertions.assertEquals(StatusEnum.Error, status);
    }

    @Test
    public void shouldThrowWhenUrlIsEmpty() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> new FileDownloader("", 1, null));
    }

    @Test
    public void shouldThrowWhenBlockSizeEqualsZero() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> new FileDownloader("test", 0, null));
    }

    @Test
    public void shouldThrowWhenBlockSizeIsLowerThanZero() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> new FileDownloader("test", -1, null));
    }

    private Path generateFile(String fileName, int sizeInMB) throws IOException {
        Path path = CommonUtils.generateFile(fileName, testDirectory, sizeInMB);
        filesToDelete.add(path);
        return path;
    }

    private FileDownloader createFileDownloader(String url, int blockSizeInMB) throws DownloadException {
        return new FileDownloader(url, blockSizeInMB, fileDownloaderDirectory);
    }
}
