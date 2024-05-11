package common.utils;

import common.exceptions.DownloadException;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

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

    public static Path joinAndRemoveFileParts(List<Path> fileParts) throws DownloadException {
        if (fileParts == null || fileParts.isEmpty()) {
            throw new DownloadException("Empty file parts list.");
        }
        if (fileParts.stream().anyMatch(file -> !file.toString().contains(".part"))) {
            throw new DownloadException("Not all files contains '.part' in path.");
        }

        String finalFileName = fileParts.get(0).getFileName().toString().replaceFirst("[.][^.]+$", "");
        Path savePath = Paths.get(String.valueOf(fileParts.get(0).getParent()), finalFileName);

        try (FileChannel out = FileChannel.open(savePath, CREATE, WRITE)) {
            for (Path filePart : fileParts) {
                System.out.println("Joining file part: " + filePart.getFileName());

                try (FileChannel in = FileChannel.open(filePart, READ)) {
                    long fileSizeInBytes = in.size();
                    for (long position = 0; position < fileSizeInBytes; ) {
                        position += in.transferTo(position, fileSizeInBytes - position, out);
                    }
                }
                removeFile(filePart);
            }
        } catch (IOException e) {
            removeFile(savePath);
            removeFiles(fileParts);
            throw new DownloadException(e, "Error while joining parts of file.");
        }

        return savePath;
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
