package common;

import common.exceptions.DownloadException;
import common.infos.EndInfoFile;
import common.utils.FilePartUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EndInfoFileTests {
    private final static ConcurrentLinkedQueue<Path> filesToDelete = new ConcurrentLinkedQueue<>();
    private final static String testDirectory = String.valueOf(Paths.get(CommonUtils.testDirectory, "EndInfoFileTests"));

    @BeforeAll
    public static void beforeAll() throws DownloadException, IOException {
        CommonUtils.beforeAll(EndInfoFileTests.testDirectory);
    }

    @AfterAll
    public static void afterAll() {
        CommonUtils.afterAll(testDirectory, new ArrayList<>(filesToDelete));
    }

    @Test
    public void shouldGenerateProperChecksums() throws IOException, DownloadException {
        // Arrange
        ArrayList<Path> files = new ArrayList<>();
        files.add(generateFile("shouldGenerateProperChecksums1", 1));
        files.add(generateFile("shouldGenerateProperChecksums2", 2));
        filesToDelete.addAll(files);

        ArrayList<String> expectedChecksums = new ArrayList<>();
        for (Path path: files) {
            expectedChecksums.add(FilePartUtils.fileChecksum(path));
        }

        Path currentTestDirectory = Paths.get(testDirectory, "shouldGenerateProperChecksums");
        Files.createDirectories(currentTestDirectory);

        // Act
        EndInfoFile endInfoFile = new EndInfoFile(currentTestDirectory.toString(), files);
        filesToDelete.add(endInfoFile.filePath);

        // Assert
        Assertions.assertEquals(expectedChecksums, endInfoFile.getChecksums());
    }

    @Test
    public void shouldSaveChecksumsToFile() throws IOException, DownloadException {
        // Arrange
        ArrayList<Path> files = new ArrayList<>();
        files.add(generateFile("shouldSaveChecksumsToFile1", 1));
        files.add(generateFile("shouldSaveChecksumsToFile2", 2));
        filesToDelete.addAll(files);

        Path currentTestDirectory = Paths.get(testDirectory, "shouldSaveChecksumsToFile");
        Files.createDirectories(currentTestDirectory);

        // Act
        EndInfoFile endInfoFile = new EndInfoFile(currentTestDirectory.toString(), files);
        filesToDelete.add(endInfoFile.filePath);

        List<String> checksums = endInfoFile.getChecksums();
        List<String> fileContent = Files.readAllLines(endInfoFile.filePath);

        // Assert
        Assertions.assertEquals(1, fileContent.size());
        String content = fileContent.get(0);
        for (String checksum: checksums) {
            Assertions.assertTrue(content.contains(checksum));
        }
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
    public void shouldThrowWhenFileDoesNotExist() {
        // Arrange
        ArrayList<Path> list = new ArrayList<>(Collections.singleton(Paths.get("notExistingFile54389732.txt")));
        Assertions.assertFalse(Files.exists(list.get(0)));

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> new EndInfoFile(testDirectory, list));
    }

    private Path generateFile(String fileName, int sizeInMB) throws IOException {
        return CommonUtils.generateFile(fileName, testDirectory, sizeInMB);
    }
}
