package common;

import common.exceptions.DownloadException;
import common.utils.FilePartUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class FilePartUtilsTests {
    private final static ConcurrentLinkedQueue<Path> filesToDelete = new ConcurrentLinkedQueue<>();
    private final static String testDirectory = String.valueOf(Paths.get(CommonUtils.testDirectory, "FilePartUtilsTests"));

    @BeforeAll
    public static void beforeAll() throws DownloadException, IOException {
        CommonUtils.beforeAll(testDirectory);
    }

    @AfterAll
    public static void afterAll() {
        CommonUtils.afterAll(testDirectory, new ArrayList<>(filesToDelete));
    }

    @Test
    public void removeFile_ShouldRemoveFile() throws IOException {
        // Arrange
        File file = new File(testDirectory, "removeFile_ShouldRemoveFile");
        Assertions.assertTrue(file.createNewFile());

        // Act
        FilePartUtils.removeFile(file.toPath());

        // Assert
        Assertions.assertFalse(file.exists());
    }

    @Test
    public void removeFile_ShouldNotThrowWhenFileDoesNotExist() {
        // Arrange
        File file = new File(testDirectory, "removeFile_ShouldNotThrowWhenFileDoesNotExist");
        Assertions.assertFalse(file.exists());

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> FilePartUtils.removeFile(file.toPath()));
    }

    @Test
    public void removeFiles_ShouldRemoveFiles() throws IOException {
        // Arrange
        List<File> files = new ArrayList<>();
        files.add(new File(testDirectory, "removeFiles_ShouldRemoveFiles1"));
        files.add(new File(testDirectory, "removeFiles_ShouldRemoveFiles2"));

        for (File file : files) {
            Assertions.assertTrue(file.createNewFile());
        }

        // Act
        FilePartUtils.removeFiles(files.stream().map(File::toPath).collect(Collectors.toList()));

        // Assert
        for (File file : files) {
            Assertions.assertFalse(file.exists());
        }
    }

    @Test
    public void removeFiles_ShouldNotThrowWhenFilesDoNotExist() {
        // Arrange
        List<File> files = new ArrayList<>();
        files.add(new File(testDirectory, "removeFiles_ShouldNotThrowWhenFilesDoNotExist1"));
        files.add(new File(testDirectory, "removeFiles_ShouldNotThrowWhenFilesDoNotExist2"));

        for (File file : files) {
            Assertions.assertFalse(file.exists());
        }

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> FilePartUtils.removeFiles(files.stream().map(File::toPath).collect(Collectors.toList())));
    }

    @Test
    public void joinAndRemoveFileParts_ShouldJoinAndRemoveFileParts() throws IOException, DownloadException {
        // Arrange
        String fileName = "joinAndRemoveFileParts_ShouldJoinAndRemoveFileParts";
        List<Path> files = new ArrayList<>();
        files.add(generateFile(fileName + ".part0"));
        files.add(generateFile(fileName + ".part1"));
        files.add(generateFile(fileName + ".part2"));

        long expectedSize = 0;
        for (Path path : files) {
            expectedSize += Files.size(path);
        }

        // Act
        Path joinedFilePath = FilePartUtils.joinAndRemoveFileParts(files);
        filesToDelete.add(joinedFilePath);

        // Assert
        for (Path path : files) {
            Assertions.assertFalse(Files.exists(path));
        }
        long joinedFileSize = Files.size(joinedFilePath);
        Assertions.assertEquals(expectedSize, joinedFileSize);
    }

    @Test
    public void joinAndRemoveFileParts_ShouldThrowOnNotExistingFileAndRemoveFiles() throws IOException {
        // Arrange
        String fileName = "joinAndRemoveFileParts_ShouldThrowOnNotExistingFileAndRemoveFiles";
        Path path1 = Paths.get(fileName + ".part0");
        Assertions.assertFalse(Files.exists(path1));

        Path path2 = generateFile(fileName + ".part1");
        Assertions.assertTrue(Files.exists(path2));

        Path joinedFilePath = Paths.get(testDirectory, fileName);
        Assertions.assertFalse(Files.exists(joinedFilePath));

        List<Path> files = new ArrayList<>();
        files.add(path1);
        files.add(path2);

        // Act
        Assertions.assertThrowsExactly(DownloadException.class, () -> FilePartUtils.joinAndRemoveFileParts(files));

        // Assert
        for (Path path : files) {
            Assertions.assertFalse(Files.exists(path));
        }
        Assertions.assertFalse(Files.exists(joinedFilePath));
    }

    @Test
    public void joinAndRemoveFileParts_ShouldJustRemovePartExtensionWhenIsSingleFile() throws IOException, DownloadException {
        // Arrange
        Path path = generateFile("joinAndRemoveFileParts_ShouldJustRemovePartExtensionWhenIsSingleFile.part0");
        Assertions.assertTrue(Files.exists(path));

        // Act
        Path joinedFilePath = FilePartUtils.joinAndRemoveFileParts(Collections.singletonList(path));
        filesToDelete.add(joinedFilePath);

        // Assert
        Assertions.assertFalse(Files.exists(path));
        Assertions.assertTrue(Files.exists(joinedFilePath));
    }

    @Test
    public void joinAndRemoveFileParts_ShouldThrowOnEmptyList() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> FilePartUtils.joinAndRemoveFileParts(new ArrayList<>()));
    }

    @Test
    public void joinAndRemoveFileParts_ShouldThrowWhenFileDoesNotContainPartInfoInName() {
        // Arrange
        Path path = Paths.get("joinAndRemoveFileParts_ShouldThrowWhenFileDoesNotContainPartInfoInName");

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> FilePartUtils.joinAndRemoveFileParts(Collections.singletonList(path)));
    }

    @Test
    public void fileChecksum_ShouldReturnTheSameChecksumForIdenticalFileContent() throws IOException, DownloadException {
        // Arrange
        Path original = generateFile("fileChecksum_ShouldReturnTheSameChecksumForIdenticalFileContent");
        Path copy = Files.copy(original, Paths.get(original.toAbsolutePath() + "Copy"));
        filesToDelete.add(copy);

        // Act
        String originalChecksum = FilePartUtils.fileChecksum(original);
        String copyChecksum = FilePartUtils.fileChecksum(copy);

        // Assert
        Assertions.assertEquals(originalChecksum, copyChecksum);
    }

    @Test
    public void fileChecksum_ShouldReturnDifferentChecksumsForDifferentFiles() throws IOException, DownloadException {
        // Arrange
        Path file1 = generateFile("fileChecksum_ShouldReturnDifferentChecksumsForDifferentFiles1");
        Path file2 = generateFile("fileChecksum_ShouldReturnDifferentChecksumsForDifferentFiles2");

        // Act
        String checksum1 = FilePartUtils.fileChecksum(file1);
        String checksum2 = FilePartUtils.fileChecksum(file2);

        // Assert
        Assertions.assertNotEquals(checksum1, checksum2);
    }

    @Test
    public void fileChecksum_ShouldThrowWhenFileDoesNotExist() {
        // Arrange
        Path path = Paths.get("fileChecksum_ShouldThrowWhenFileDoesNotExist");
        Assertions.assertFalse(Files.exists(path));

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> FilePartUtils.fileChecksum(path));
    }

    @Test
    public void fileChecksum_ShouldThrowWhenFileIsEmpty() throws IOException {
        // Arrange
        Path path = Paths.get(testDirectory, "fileChecksum_ShouldThrowWhenFileIsEmpty");
        Files.createFile(path);
        filesToDelete.add(path);

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> FilePartUtils.fileChecksum(path));
    }

    @Test
    public void megabytesToBytes_ShouldReturnProperValue() {
        // Arrange
        int megabytes = 10;

        // Act
        long bytes = FilePartUtils.megabytesToBytes(megabytes);

        // Assert
        Assertions.assertEquals(megabytes * 1024 * 1024, bytes);
    }

    @Test
    public void megabytesToBytes_ShouldReturnZeros() {
        // Arrange
        ArrayList<Integer> megabytes = new ArrayList<>();
        megabytes.add(0);
        megabytes.add(-1);

        // Act
        List<Long> bytes = megabytes.stream().map(FilePartUtils::megabytesToBytes).collect(Collectors.toList());

        // Assert
        for (Long l : bytes) {
            Assertions.assertEquals(0L, l);
        }
    }

    @Test
    public void bytesToMegabytes_ShouldReturnCeilValues() {
        // Arrange
        int fiveMB = 5;
        long fiveMBInBytes = fiveMB * 1024 * 1024;
        HashMap<Long, Integer> bytesAndMB = new HashMap<>();
        bytesAndMB.put(fiveMBInBytes, fiveMB);
        bytesAndMB.put(fiveMBInBytes - 1, fiveMB);
        bytesAndMB.put(fiveMBInBytes + 1, fiveMB + 1);
        bytesAndMB.put(1L, 1);

        // Act
        List<Integer> results = bytesAndMB.keySet().stream().map(FilePartUtils::bytesToMegabytes).collect(Collectors.toList());

        // Assert
        for (int i = 0; i < bytesAndMB.size(); i++) {
            Assertions.assertEquals(bytesAndMB.values().toArray()[i], results.get(i));
        }
    }

    @Test
    public void bytesToMegabytes_ShouldReturnZeros() {
        // Arrange
        ArrayList<Integer> bytes = new ArrayList<>();
        bytes.add(0);
        bytes.add(-1);

        // Act
        List<Integer> megabytes = bytes.stream().map(FilePartUtils::bytesToMegabytes).collect(Collectors.toList());

        // Assert
        for (int mb : megabytes) {
            Assertions.assertEquals(0, mb);
        }
    }

    private Path generateFile(String fileName) throws IOException {
        Path path = CommonUtils.generateFile(fileName, testDirectory, 1);
        filesToDelete.add(path);
        return path;
    }
}
