package common;

import common.exceptions.DownloadException;
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
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChecksumServiceTests {
    private final static ConcurrentLinkedQueue<Path> filesToDelete = new ConcurrentLinkedQueue<>();
    private final static String testDirectory = String.valueOf(Paths.get(CommonUtils.testDirectory, "ChecksumServiceTests"));

    @BeforeAll
    public static void beforeAll() throws DownloadException, IOException {
        CommonUtils.beforeAll(testDirectory);
    }

    @AfterAll
    public static void afterAll() {
        CommonUtils.afterAll(testDirectory, new ArrayList<>(filesToDelete));
    }

    @Test
    public void fileChecksum_ShouldReturnTheSameChecksumForIdenticalFileContent() throws IOException, DownloadException {
        // Arrange
        Path original = generateFile("fileChecksum_ShouldReturnTheSameChecksumForIdenticalFileContent");
        Path copy = Files.copy(original, Paths.get(original.toAbsolutePath() + "Copy"));
        filesToDelete.add(copy);

        // Act
        ChecksumService checksumService = new ChecksumService();
        checksumService.addFileToProcess(original);
        checksumService.addFileToProcess(copy);

        String originalChecksum = checksumService.getChecksums().get(0);
        String copyChecksum = checksumService.getChecksums().get(1);

        // Assert
        Assertions.assertEquals(originalChecksum, copyChecksum);
    }

    @Test
    public void fileChecksum_ShouldReturnDifferentChecksumsForDifferentFiles() throws IOException, DownloadException {
        // Arrange
        Path file1 = generateFile("fileChecksum_ShouldReturnDifferentChecksumsForDifferentFiles1");
        Path file2 = generateFile("fileChecksum_ShouldReturnDifferentChecksumsForDifferentFiles2");

        // Act
        ChecksumService checksumService = new ChecksumService();
        checksumService.addFileToProcess(file1);
        checksumService.addFileToProcess(file2);
        String checksum1 = checksumService.getChecksums().get(0);
        String checksum2 = checksumService.getChecksums().get(1);

        // Assert
        Assertions.assertNotEquals(checksum1, checksum2);
    }

    @Test
    public void fileChecksum_ShouldThrowWhenFileDoesNotExist() {
        // Arrange
        Path path = Paths.get("fileChecksum_ShouldThrowWhenFileDoesNotExist");
        Assertions.assertFalse(Files.exists(path));

        // Act
        ChecksumService checksumService = new ChecksumService();
        checksumService.addFileToProcess(path);

        // Assert
        Assertions.assertThrowsExactly(DownloadException.class, checksumService::getChecksums);
    }

    @Test
    public void fileChecksum_ShouldThrowWhenFileIsEmpty() throws IOException {
        // Arrange
        Path path = Paths.get(testDirectory, "fileChecksum_ShouldThrowWhenFileIsEmpty");
        Files.createFile(path);
        filesToDelete.add(path);

        // Act
        ChecksumService checksumService = new ChecksumService();
        checksumService.addFileToProcess(path);

        // Assert
        Assertions.assertThrowsExactly(DownloadException.class, checksumService::getChecksums);
    }

    private Path generateFile(String fileName) throws IOException {
        Path path = CommonUtils.generateFile(fileName, testDirectory, 1);
        filesToDelete.add(path);
        return path;
    }
}
