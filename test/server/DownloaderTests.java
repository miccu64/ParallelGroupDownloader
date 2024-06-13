package server;

import common.exceptions.DownloadException;
import common.models.StatusEnum;
import common.services.FileService;
import common.utils.FilePartUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static testingUtils.CommonTestingUtils.generateFile;

public class DownloaderTests {
    @Test
    public void shouldOriginalAndDownloadedFilesMatchChecksums() throws IOException, DownloadException {
        // Arrange
        Path filePathToDownload = generateFile(5);

        Path finalFilePath = Files.createTempFile(null, null);
        FileService fileService = new FileService(finalFilePath);

        // Act
        FileDownloader fileDownloader = createFileDownloader(filePathToDownload.toString(), 2);
        StatusEnum result = fileDownloader.call();
        Assertions.assertEquals(StatusEnum.Success, result);

        // join files
        for (Path p : fileDownloader.getProcessedFiles()) {
            fileService.addFileToProcess(p);
        }
        fileService.waitForChecksums();

        // calculate checksums of joined files
        fileService = new FileService(Files.createTempFile(null, null));
        fileService.addFileToProcess(finalFilePath);
        fileService.addFileToProcess(filePathToDownload);

        String downloadedChecksum = fileService.waitForChecksums().get(0);
        String originalChecksum = fileService.waitForChecksums().get(1);

        // Assert
        Assertions.assertEquals(originalChecksum, downloadedChecksum);
    }

    @Test
    public void shouldDivideDownloadedFileWithExactPartSize() throws IOException, DownloadException {
        // Arrange
        Path filePathToDownload = generateFile(3);
        int blockSizeInMB = 1;

        // Act
        FileDownloader fileDownloader = createFileDownloader(filePathToDownload.toString(), blockSizeInMB);
        StatusEnum result = fileDownloader.call();

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
        Path filePathToDownload = generateFile(5);

        Path finalFilePath = Files.createTempFile(null, null);
        FileService fileService = new FileService(finalFilePath);

        // Act
        FileDownloader fileDownloader = createFileDownloader(filePathToDownload.toString(), 2);
        StatusEnum result = fileDownloader.call();
        Assertions.assertEquals(StatusEnum.Success, result);

        List<Path> processedFiles = fileDownloader.getProcessedFiles();
        for (Path p : processedFiles) {
            fileService.addFileToProcess(p);
        }

        long sizeInBytes = 0;
        for (Path filePart : processedFiles) {
            sizeInBytes += Files.size(filePart);
        }

        fileService.waitForChecksums();

        long sizeInBytesAfterJoin = Files.size(finalFilePath);

        // Assert
        Assertions.assertEquals(sizeInBytes, sizeInBytesAfterJoin);
    }

    @Test
    public void shouldFileDownloaderGetProperLocalFileSize() throws IOException, DownloadException {
        // Arrange
        Path filePathToDownload = generateFile(1);
        int expectedSize = FilePartUtils.bytesToMegabytes(Files.size(filePathToDownload));

        // Act
        FileDownloader fileDownloader = createFileDownloader(filePathToDownload.toString(), 1);
        long fileDownloaderSize = fileDownloader.getFileSizeInMB();

        // Assert
        Assertions.assertEquals(expectedSize, fileDownloaderSize);
    }

    @Test
    public void shouldReturnProperFileNameFromLocalFile() throws IOException, DownloadException {
        // Arrange
        Path filePathToDownload = generateFile(1);

        // Act
        FileDownloader fileDownloader = createFileDownloader(filePathToDownload.toString(), 1);
        String fileName = fileDownloader.getFileName();

        // Assert
        Assertions.assertEquals(filePathToDownload.getFileName().toString(), fileName);
    }

    @Test
    public void shouldReturnProperFileNameFromUrl() throws DownloadException, IOException {
        // Arrange
        String expectedFileName = "file.txt";
        String url = "http://not-existing-url-32176573546.pll/" + expectedFileName;

        // Act
        FileDownloader fileDownloader = createFileDownloader(url, 1);
        String fileName = fileDownloader.getFileName();

        // Assert
        Assertions.assertEquals(expectedFileName, fileName);
    }

    @Test
    public void shouldNotReturnSizeWhenCannotGetIt() throws DownloadException, IOException {
        // Arrange
        String url = "http://not-existing-url-32176573546.pl/file.txt";

        // Act
        FileDownloader fileDownloader = createFileDownloader(url, 1);
        long size = fileDownloader.getFileSizeInMB();

        // Assert
        Assertions.assertEquals(0, size);
    }

    @Test
    public void shouldWorkWithDifferentPathFormats() throws IOException, DownloadException {
        // Arrange
        int fileSize = 1;
        Path filePathToDownload = generateFile(fileSize);

        List<String> paths = new ArrayList<>();
        paths.add(filePathToDownload.toString());
        paths.add(filePathToDownload.toAbsolutePath().toString());
        paths.add(filePathToDownload.toUri().toString());
        paths.add(filePathToDownload.toUri().toURL().toString());

        // Act / Assert
        for (String path : paths) {
            FileDownloader fileDownloader = createFileDownloader(path, 2);
            Assertions.assertEquals(fileSize, fileDownloader.getFileSizeInMB());
            Assertions.assertEquals(filePathToDownload.getFileName().toString(), fileDownloader.getFileName());
        }
    }

    @Test
    public void shouldThrowWhenDirectoryGiven() throws IOException {
        // Arrange
        Path directory = Files.createTempDirectory(null);

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> createFileDownloader(directory.toString(), 1));
    }

    @Test
    public void shouldThrowWhenFileNotExists() {
        // Arrange
        Path notExistingFile = Paths.get("shouldThrowWhenFileNotExists.file").toAbsolutePath();
        File f = notExistingFile.toFile();
        Assertions.assertFalse(f.exists());

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> createFileDownloader(notExistingFile.toString(), 1));
    }

    @Test
    public void shouldThrowWhenIsMalformedLocalFileUrl() {
        // Arrange
        String malformedUrl = Paths.get("shouldThrowWhenIsMalformedLocalFileUrl.file").toString();

        // Act
        Assertions.assertThrowsExactly(DownloadException.class, () -> createFileDownloader(malformedUrl, 1));
    }

    @Test
    public void shouldReturnErrorWhenUrlDoesNotExist() throws DownloadException {
        // Arrange
        String malformedUrl = "http://not-existing-url-32176573546.pl/file.txt";

        // Act
        StatusEnum status = new FileDownloader(malformedUrl, null, null, 1).call();

        // Assert
        Assertions.assertEquals(StatusEnum.Error, status);
    }

    @Test
    public void shouldThrowWhenUrlIsEmpty() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> new FileDownloader("", null, null, 1));
    }

    @Test
    public void shouldThrowWhenBlockSizeEqualsZero() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> new FileDownloader("test", null, null, 0));
    }

    @Test
    public void shouldThrowWhenBlockSizeIsLowerThanZero() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> new FileDownloader("test", null, null, -1));
    }

    private FileDownloader createFileDownloader(String url, int blockSizeInMB) throws DownloadException, IOException {
        return new FileDownloader(url, Files.createTempDirectory(null).toString(), null, blockSizeInMB);
    }
}
