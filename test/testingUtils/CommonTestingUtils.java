package testingUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class CommonTestingUtils {
    public static Path generateFile(int sizeInMB) throws IOException {
        Path filePath = Files.createTempFile(null, null);
        byte[] bytes = new byte[1024 * 1024 * sizeInMB];
        new Random().nextBytes(bytes);
        Files.write(filePath, bytes);

        filePath.toFile().deleteOnExit();

        return filePath;
    }
}
