package common.utils;

import common.exceptions.DownloadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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

    public static Path joinAndRemoveFileParts(List<Path> fileParts) throws DownloadException {
        if (fileParts == null || fileParts.isEmpty()) {
            throw new DownloadException("Empty file parts list.");
        }
        if (fileParts.stream().anyMatch(file -> !file.toString().contains(".part"))) {
            throw new DownloadException("Not all files contains '.part' in path.");
        }

        String finalFileName = fileParts.get(0).getFileName().toString().replaceFirst("[.][^.]+$", "");
        Path savePath = Paths.get(String.valueOf(fileParts.get(0).getParent()), finalFileName);

        try (OutputStream out = Files.newOutputStream(savePath)) {
            for (Path filePart : fileParts) {
                System.out.println(filePart);
                Files.copy(filePart, out);
                removeFile(filePart);
            }
        } catch (IOException e) {
            removeFiles(fileParts);
            throw new DownloadException(e, "Error while joining parts of file.");
        }

        return savePath;
    }

    public static String fileChecksum(Path filePath) throws DownloadException {
        if (filePath.toFile().length() < 1) {
            throw new DownloadException("Empty file.");
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (InputStream inputStream = Files.newInputStream(filePath);
                 DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest)) {
                byte[] buffer = new byte[50000000];
                while (digestInputStream.read(buffer) != -1) {
                }
            }
            byte[] checksum = messageDigest.digest();
            return Base64.getEncoder().encodeToString(checksum);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new DownloadException(e, "Could not create file checksum.");
        }
    }

    public static long megabytesToBytes(int megabytes) {
        if (megabytes < 0) {
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
