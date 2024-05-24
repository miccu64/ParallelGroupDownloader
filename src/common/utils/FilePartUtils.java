package common.utils;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilePartUtils {
    public static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    public static void removeFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
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

    public static Path generateFilePartPath(String downloadDirectory, String fileName, int expectedSizeInMB) {
        Path path = Paths.get(downloadDirectory, fileName);
        if (!isWindows) {
            Path ramdisk = Paths.get("/dev/shm");
            int sizeInMBWithMargin = expectedSizeInMB + 128;
            if (Files.exists(ramdisk) && checkFreeSpace(ramdisk, sizeInMBWithMargin)) {
                path = Paths.get(String.valueOf(ramdisk), fileName);
            }
        }

        path.toFile().deleteOnExit();
        return path.toAbsolutePath();
    }

    public static boolean checkFreeSpace(Path path, int expectedSizeInMB) {
        try {
            FileStore store = Files.getFileStore(path);
            return store.getUsableSpace() > (FilePartUtils.megabytesToBytes(expectedSizeInMB));
        } catch (IOException ignored) {
            return false;
        }
    }
}
