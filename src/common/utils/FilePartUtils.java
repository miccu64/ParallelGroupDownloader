package common.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FilePartUtils {
    public static void removeFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    public static void removeFiles(List<Path> filePaths) {
        for (Path path : filePaths) {
            removeFile(path);
        }
    }

    public static long megabytesToBytes(int megabytes) {
        if (megabytes <= 0) {
            return 0;
        }
        return ((long) megabytes) * 1024 * 1024;
    }

    public static int bytesToMegabytes(long bytes) {
        if (bytes <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) bytes / (1024 * 1024));
    }
}
