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
import java.util.Collections;
import java.util.List;

public class FilePartUtils {
    public static void removeFiles(List<Path> filePaths) {
        for (Path path : filePaths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
        }
    }

    public static void joinAndDeleteFileParts(List<Path> fileParts) throws DownloadException {
        String finalFileName = fileParts.get(0).getFileName().toString().replaceFirst("[.][^.]+$", "");
        Path savePath = Paths.get(String.valueOf(fileParts.get(0).getParent()), finalFileName);

        System.out.println(savePath);
        try (OutputStream out = Files.newOutputStream(savePath)) {
            for (Path filePart : fileParts) {
                System.out.println(filePart);
                Files.copy(filePart, out);
                removeFiles(Collections.singletonList(filePart));
            }
        } catch (IOException e) {
            removeFiles(fileParts);
            throw new DownloadException(e, "Error while joining parts of file");
        }
    }

    public static String fileChecksum(Path filePath) throws DownloadException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(filePath);
                 DigestInputStream ignored = new DigestInputStream(is, md)) {
                byte[] checksum = md.digest();
                return Base64.getEncoder().encodeToString(checksum);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new DownloadException(e, "Could not create file checksum");
        }
    }
}
