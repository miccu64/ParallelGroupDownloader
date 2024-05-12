package common;

import common.exceptions.DownloadException;
import common.services.FileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static utils.CommonUtils.generateFile;

public class FileServiceTests {
    @Test
    public void fileChecksum_ShouldReturnTheSameChecksumForIdenticalFileContent() throws IOException, DownloadException {
        // Arrange
        Path original = generateFile(1);
        Path copy = Files.copy(original, Paths.get(original.toAbsolutePath() + "Copy"));
        copy.toFile().deleteOnExit();

        // Act
        FileService fileService = createFileService();
        fileService.addFileToProcess(original);
        fileService.addFileToProcess(copy);

        String originalChecksum = fileService.waitForChecksums().get(0);
        String copyChecksum = fileService.waitForChecksums().get(1);

        // Assert
        Assertions.assertEquals(originalChecksum, copyChecksum);
    }

    @Test
    public void fileChecksum_ShouldReturnDifferentChecksumsForDifferentFiles() throws IOException, DownloadException {
        // Arrange
        Path file1 = generateFile(1);
        Path file2 = generateFile(1);

        // Act
        FileService fileService = createFileService();
        fileService.addFileToProcess(file1);
        fileService.addFileToProcess(file2);
        String checksum1 = fileService.waitForChecksums().get(0);
        String checksum2 = fileService.waitForChecksums().get(1);

        // Assert
        Assertions.assertNotEquals(checksum1, checksum2);
    }

    @Test
    public void fileChecksum_ShouldThrowWhenFileDoesNotExist() throws DownloadException, IOException {
        // Arrange
        Path path = Paths.get("fileChecksum_ShouldThrowWhenFileDoesNotExist");
        Assertions.assertFalse(Files.exists(path));

        // Act
        FileService fileService = createFileService();
        fileService.addFileToProcess(path);

        // Assert
        Assertions.assertThrowsExactly(DownloadException.class, fileService::waitForChecksums);
    }

    private FileService createFileService() throws IOException, DownloadException {
        return new FileService(Files.createTempFile(null, null));
    }
}
