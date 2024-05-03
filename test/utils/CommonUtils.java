package utils;

import common.exceptions.DownloadException;
import common.utils.PrepareDownloadUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class CommonUtils {
    public final static String testDirectory = "filesTest";

    public static void beforeAll(String testDirectory) throws DownloadException, IOException {
        PrepareDownloadUtils.initProgram();

        Files.createDirectories(Paths.get(CommonUtils.testDirectory));
        Files.createDirectories(Paths.get(testDirectory));
    }

    public static void afterAll(String subdirectory, List<Path> filesToDelete) {
        for (Path path : filesToDelete) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
        }

        try (Stream<Path> pathStream = Files.walk(Paths.get(testDirectory, subdirectory))) {
            pathStream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        boolean ignored = file.delete();
                    });
        } catch (IOException ignored) {
        }
    }

    public static Path generateFile(String fileName, String testDirectory, int sizeInMB) throws IOException {
        Path filePath = Paths.get(testDirectory, fileName);
        byte[] bytes = new byte[1024 * 1024 * sizeInMB];
        new Random(22).nextBytes(bytes);
        Files.write(filePath, bytes);

        return filePath;
    }
}
