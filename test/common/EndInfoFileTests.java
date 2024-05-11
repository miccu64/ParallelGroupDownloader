package common;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;
import common.infos.EndInfoFile;
import common.services.ChecksumService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.CommonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EndInfoFileTests {
    private final static ConcurrentLinkedQueue<Path> filesToDelete = new ConcurrentLinkedQueue<>();
    private final static String testDirectory = String.valueOf(Paths.get(CommonUtils.testDirectory, "EndInfoFileTests"));

    @BeforeAll
    public static void beforeAll() throws IOException {
        CommonUtils.beforeAll(testDirectory);
    }

    @AfterAll
    public static void afterAll() {
        CommonUtils.afterAll(testDirectory, new ArrayList<>(filesToDelete));
    }

    @Test
    public void shouldSaveChecksumsToFile() throws IOException, DownloadException {
        // Arrange
        String fileName = "shouldSaveChecksumsToFile";
        ArrayList<Path> files = new ArrayList<>();
        files.add(generateFile(fileName + "1", 1));
        files.add(generateFile(fileName + "2", 2));

        Path currentTestDirectory = Paths.get(testDirectory, fileName);
        Files.createDirectories(currentTestDirectory);

        ChecksumService checksumService = new ChecksumService();
        for (Path path : files) {
            checksumService.addFileToProcess(path);
        }
        List<String> expectedChecksums = checksumService.getChecksums();

        // Act
        EndInfoFile endInfoFile = new EndInfoFile(currentTestDirectory.toString(), expectedChecksums);
        filesToDelete.add(endInfoFile.filePath);

        List<String> checksums = endInfoFile.getChecksums();
        List<String> fileContent = Files.readAllLines(endInfoFile.filePath);

        // Assert
        Assertions.assertEquals(1, fileContent.size());
        String content = fileContent.get(0);
        for (String checksum : checksums) {
            Assertions.assertTrue(content.contains(checksum));
        }
    }

    @Test
    public void shouldInitValuesAndLoadedFromFileValuesBeTheSame() throws IOException, DownloadException, InfoFileException {
        // Arrange
        String fileName = "shouldInitValuesAndLoadedFromFileValuesBeTheSame";
        ArrayList<Path> files = new ArrayList<>();
        files.add(generateFile(fileName + "1", 1));
        files.add(generateFile(fileName + "2", 2));

        Path currentTestDirectory = Paths.get(testDirectory, fileName);
        Files.createDirectories(currentTestDirectory);

        ChecksumService checksumService = new ChecksumService();
        for (Path path : files) {
            checksumService.addFileToProcess(path);
        }
        List<String> checksums = checksumService.getChecksums();

        // Act
        EndInfoFile endInfoFile1 = new EndInfoFile(currentTestDirectory.toString(), checksums);
        filesToDelete.add(endInfoFile1.filePath);
        EndInfoFile endInfoFile2 = new EndInfoFile(endInfoFile1.filePath);

        // Assert
        Assertions.assertEquals(endInfoFile1.filePath, endInfoFile2.filePath);
        Assertions.assertEquals(endInfoFile1.getChecksums(), endInfoFile2.getChecksums());
        Assertions.assertEquals(endInfoFile1.toString(), endInfoFile2.toString());
    }

    @Test
    public void shouldThrowWhenSaveDirectoryIsEmpty() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> new EndInfoFile("", new ArrayList<>()));
    }

    @Test
    public void shouldThrowWhenSaveDirectoryDoesNotExist() {
        // Arrange
        Path path = Paths.get("notExistingDirectory-534534654778");
        Assertions.assertFalse(Files.exists(path));

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> new EndInfoFile(path.toString(), new ArrayList<>()));
    }

    @Test
    public void shouldThrowWhenNoFilesAreGiven() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> new EndInfoFile(testDirectory, new ArrayList<>()));
    }

    @Test
    public void shouldThrowWhenNoChecksumsAreGiven() {
        // Arrange
        List<String> checksums = new ArrayList<>();

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> new EndInfoFile(testDirectory, checksums));
    }

    private Path generateFile(String fileName, int sizeInMB) throws IOException {
        Path path = CommonUtils.generateFile(fileName, testDirectory, sizeInMB);
        filesToDelete.add(path);
        return path;
    }
}
