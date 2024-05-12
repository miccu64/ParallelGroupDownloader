package common;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;
import common.infos.EndInfoFile;
import common.services.FileService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static utils.CommonUtils.generateFile;

public class EndInfoFileTests {
    @Test
    public void shouldSaveChecksumsToFile() throws IOException, DownloadException {
        // Arrange
        ArrayList<Path> files = new ArrayList<>();
        files.add(generateFile(1));
        files.add(generateFile(2));

        Path currentTestDirectory = Files.createTempDirectory(null);

        FileService fileService = new FileService(Files.createTempFile(null, null));
        for (Path path : files) {
            fileService.addFileToProcess(path);
        }
        List<String> expectedChecksums = fileService.waitForChecksums();

        // Act
        EndInfoFile endInfoFile = new EndInfoFile(currentTestDirectory.toString(), expectedChecksums);

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
        ArrayList<Path> files = new ArrayList<>();
        files.add(generateFile(1));
        files.add(generateFile(2));

        Path currentTestDirectory = Files.createTempDirectory(null);

        FileService fileService = new FileService(Files.createTempFile(null, null));
        for (Path path : files) {
            fileService.addFileToProcess(path);
        }
        List<String> checksums = fileService.waitForChecksums();

        // Act
        EndInfoFile endInfoFile1 = new EndInfoFile(currentTestDirectory.toString(), checksums);
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
    public void shouldThrowWhenNoFilesAreGiven() throws IOException {
        // Arrange
        String path = Files.createTempDirectory(null).toString();

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> new EndInfoFile(path, new ArrayList<>()));
    }

    @Test
    public void shouldThrowWhenNoChecksumsAreGiven() throws IOException {
        // Arrange
        String path = Files.createTempDirectory(null).toString();
        List<String> checksums = new ArrayList<>();

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> new EndInfoFile(path, checksums));
    }
}
