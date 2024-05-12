package common;

import common.utils.FilePartUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FilePartUtilsTests {
    @Test
    public void removeFile_ShouldRemoveFile() throws IOException {
        // Arrange
        Path path = Files.createTempFile(null, null);

        // Act
        FilePartUtils.removeFile(path);

        // Assert
        Assertions.assertFalse(Files.exists(path));
    }

    @Test
    public void removeFile_ShouldNotThrowWhenFileDoesNotExist() {
        // Arrange
        File file = new File("removeFile_ShouldNotThrowWhenFileDoesNotExist");
        Assertions.assertFalse(file.exists());

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> FilePartUtils.removeFile(file.toPath()));
    }

    @Test
    public void removeFiles_ShouldRemoveFiles() throws IOException {
        // Arrange
        List<Path> files = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            files.add(Files.createTempFile(null, null));
        }

        // Act
        FilePartUtils.removeFiles(files);

        // Assert
        for (Path file : files) {
            Assertions.assertFalse(Files.exists(file));
        }
    }

    @Test
    public void removeFiles_ShouldNotThrowWhenFilesDoNotExist() {
        // Arrange
        List<File> files = new ArrayList<>();
        files.add(new File("removeFiles_ShouldNotThrowWhenFilesDoNotExist1"));
        files.add(new File("removeFiles_ShouldNotThrowWhenFilesDoNotExist2"));

        for (File file : files) {
            Assertions.assertFalse(file.exists());
        }

        // Act / Assert
        Assertions.assertDoesNotThrow(() -> FilePartUtils.removeFiles(files.stream().map(File::toPath).collect(Collectors.toList())));
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
}
