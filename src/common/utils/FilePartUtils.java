package common.utils;

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
    public static void removeFiles(List<Path> filePaths) {
        for (Path path : filePaths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean joinAndDeleteFileParts(List<Path> fileParts) {
        String finalFileName = fileParts.get(0).getFileName().toString().replaceFirst("[.][^.]+$", "");
        Path savePath = Paths.get(String.valueOf(fileParts.get(0).getParent()), finalFileName);

        System.out.println(savePath);
        try (OutputStream out = Files.newOutputStream(savePath)) {
            for (Path filePart : fileParts) {
                System.out.println(filePart);
                Files.copy(filePart, out);
            }
        } catch (IOException e) {
            return handleException(e, "Error while joining parts of file", fileParts);
        }

        removeFiles(fileParts);
        return true;
    }

    public static String fileChecksum(Path filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(filePath);
                 DigestInputStream ignored = new DigestInputStream(is, md)) {
                byte[] checksum = md.digest();
                return Base64.getEncoder().encodeToString(checksum);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            handleException(e, "Could not create file checksum", null);
            return "";
        }
    }

    private static boolean handleException(Exception e, String message, List<Path> fileParts) {
        System.out.println(message);
        e.printStackTrace(System.out);

        if (fileParts != null) {
            removeFiles(fileParts);
        }

        return false;
    }
}
