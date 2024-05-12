package common;

import common.exceptions.DownloadException;
import common.infos.StartInfoFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StartInfoFileTests {
    @Test
    public void shouldCreateProperData() throws IOException, DownloadException {
        // Arrange
        String directory = String.valueOf(Files.createTempDirectory(null));
        String url = "http://not-existing-url-3217657kk3546.pl/file.txt";
        String fileName = "testFile231.zip";
        int summarySizeInMB = 155;
        int partSizeInMB = 22;

        // Act
        StartInfoFile info = new StartInfoFile(directory, url, fileName, summarySizeInMB, partSizeInMB);
        String data = info.toString();

        // Assert
        Assertions.assertTrue(data.contains(url));
        Assertions.assertTrue(data.contains(fileName));
        Assertions.assertTrue(data.contains(String.valueOf(summarySizeInMB)));
        Assertions.assertTrue(data.contains(String.valueOf(partSizeInMB)));
    }

    @Test
    public void shouldThrowOnWrongConstructorParams() {
        Assertions.assertThrowsExactly(DownloadException.class, () -> new StartInfoFile("", "test", "test", 11, 11));
        Assertions.assertThrowsExactly(DownloadException.class, () -> new StartInfoFile("test", "", "test", 11, 11));
        Assertions.assertThrowsExactly(DownloadException.class, () -> new StartInfoFile("test", "test", "", 11, 11));
        Assertions.assertThrowsExactly(DownloadException.class, () -> new StartInfoFile("test", "test", "test", -1, 11));
        Assertions.assertThrowsExactly(DownloadException.class, () -> new StartInfoFile("test", "test", "test", 11, 0));
        Assertions.assertThrowsExactly(DownloadException.class, () -> new StartInfoFile("test", "test", "test", 11, -1));
    }

    @Test
    public void shouldReturnExpectedValues() throws DownloadException, IOException {
        // Arrange
        String directory = String.valueOf(Files.createTempDirectory(null));
        String url = "testUrl";
        String fileName = "testFile";
        int summarySizeInMB = 11;
        int partSizeInMB = 22;

        Path expectedStartInfoFilePath = Paths.get(directory, "startInfo.txt");

        // Act
        StartInfoFile info = new StartInfoFile(directory, url, fileName, summarySizeInMB, partSizeInMB);

        // Assert
        Assertions.assertEquals(expectedStartInfoFilePath, info.filePath);
        Assertions.assertEquals(url, info.url);
        Assertions.assertEquals(fileName, info.fileName);
        Assertions.assertEquals(summarySizeInMB, info.summarySizeInMB);
        Assertions.assertEquals(partSizeInMB, info.partSizeInMB);
    }

    @Test
    public void shouldSavedFileAndLoadedFileHaveTheSameContent() throws DownloadException, IOException {
        // Arrange
        String url = "testUrl";
        String fileName = "testFile";
        int summarySizeInMB = 11;
        int partSizeInMB = 44;

        String directory = String.valueOf(Files.createTempDirectory(null));

        // Act
        StartInfoFile info1 = new StartInfoFile(directory, url, fileName, summarySizeInMB, partSizeInMB);

        Path copyInfoPath = Paths.get(directory, "copyInfo.txt");
        Files.copy(info1.filePath, copyInfoPath);

        StartInfoFile info2 = new StartInfoFile(copyInfoPath);

        // Assert
        Assertions.assertEquals(info1.toString(), info2.toString());
    }

    @Test
    public void shouldThrowWhenFileIsNotProperStartFile() throws IOException {
        // Arrange
        Path filePath = Files.createTempFile(null, null);
        Files.write(filePath, "someText".getBytes());

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> new StartInfoFile(filePath));
    }
}
