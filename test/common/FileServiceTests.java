package common;

import common.exceptions.DownloadException;
import common.services.FileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.CommonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static utils.CommonUtils.generateFile;

public class FileServiceTests {
    @Test
    public void waitForChecksums_ShouldReturnTheSameChecksumForIdenticalFileContent() throws IOException, DownloadException {
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
    public void waitForChecksums_ShouldReturnDifferentChecksumsForDifferentFiles() throws IOException, DownloadException {
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
    public void waitForChecksums_ShouldThrowWhenFileDoesNotExist() throws DownloadException, IOException {
        // Arrange
        Path path = Paths.get("waitForChecksums_ShouldThrowWhenFileDoesNotExist");
        Assertions.assertFalse(Files.exists(path));

        // Act
        FileService fileService = createFileService();
        fileService.addFileToProcess(path);

        // Assert
        Assertions.assertThrowsExactly(DownloadException.class, fileService::waitForChecksums);
    }

    @Test
    public void waitForChecksums_ShouldThrowWhenFileDoesNotExists() throws DownloadException, IOException {
        // Arrange
        long expectedSize = 0;
        List<Path> files = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Path path = CommonUtils.generateFile(1);
            files.add(path);
            expectedSize += Files.size(path);
        }
        Path joinedFile = Files.createTempFile(null, null);

        // Act
        FileService fileService = new FileService(joinedFile);
        for (Path path : files) {
            fileService.addFileToProcess(path);
        }
        fileService.waitForFilesJoin();
        fileService.waitForFilesJoin();
        fileService.waitForFilesJoin();

        // Assert
        Assertions.assertEquals(expectedSize, Files.size(joinedFile));
    }

    private FileService createFileService() throws IOException, DownloadException {
        return new FileService(Files.createTempFile(null, null));
    }
}
