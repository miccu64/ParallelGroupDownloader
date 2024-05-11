package common;

import common.exceptions.DownloadException;
import common.infos.StartInfoFile;
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

public class StartInfoFileTests {
    private final static ConcurrentLinkedQueue<Path> filesToDelete = new ConcurrentLinkedQueue<>();
    private final static String testDirectory = String.valueOf(Paths.get(CommonUtils.testDirectory, "StartInfoFileTests"));

    @BeforeAll
    public static void beforeAll() throws DownloadException, IOException {
        CommonUtils.beforeAll(testDirectory);
    }

    @AfterAll
    public static void afterAll() {
        CommonUtils.afterAll(testDirectory, new ArrayList<>(filesToDelete));
    }

    @Test
    public void shouldCreateProperData() throws IOException, DownloadException {
        // Arrange
        String url = "http://not-existing-url-3217657kk3546.pl/file.txt";
        String fileName = "testFile231.zip";
        int summarySizeInMB = 155;
        int partSizeInMB = 22;

        String directory = createSubdirectory("shouldCreateProperFile");

        // Act
        StartInfoFile info = new StartInfoFile(directory, url, fileName, summarySizeInMB, partSizeInMB);
        filesToDelete.add(info.filePath);
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
        String url = "testUrl";
        String fileName = "testFile";
        int summarySizeInMB = 11;
        int partSizeInMB = 22;

        String directory = createSubdirectory("shouldReturnExpectedValues");
        Path expectedStartInfoFilePath = Paths.get(directory, "startInfo.txt");

        // Act
        StartInfoFile info = new StartInfoFile(directory, url, fileName, summarySizeInMB, partSizeInMB);
        filesToDelete.add(info.filePath);

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

        String directory = createSubdirectory("shouldSavedFileAndLoadedFileHaveTheSameContent");

        // Act
        StartInfoFile info1 = new StartInfoFile(directory, url, fileName, summarySizeInMB, partSizeInMB);
        filesToDelete.add(info1.filePath);

        Path copyInfoPath = Paths.get(directory, "copyInfo.txt");
        filesToDelete.add(copyInfoPath);
        Files.copy(info1.filePath, copyInfoPath);

        StartInfoFile info2 = new StartInfoFile(copyInfoPath);

        // Assert
        Assertions.assertEquals(info1.toString(), info2.toString());
    }

    @Test
    public void shouldThrowWhenFileIsNotProperStartFile() throws IOException {
        // Arrange
        Path filePath = Paths.get(testDirectory, "shouldNotInstantiateWhenFileIsNotProperStartFile.txt");
        filesToDelete.add(filePath);
        Files.write(filePath, "someText".getBytes());

        // Act / Assert
        Assertions.assertThrowsExactly(DownloadException.class, () -> new StartInfoFile(filePath));
    }

    private static String createSubdirectory(String subdirectory) throws IOException {
        return String.valueOf(Files.createDirectories(Paths.get(testDirectory, subdirectory)));
    }
}
