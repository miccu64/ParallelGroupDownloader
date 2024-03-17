package common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class FilePartUtils {
    public static void removeFileParts(List<Path> filePartPaths) {
        for (Path path : filePartPaths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean joinAndDeleteFileParts(Path finalFile, List<Path> fileParts) {
        try {
            Files.deleteIfExists(finalFile);
            Files.createFile(finalFile);
        } catch (IOException e) {
            return handleException(e, "Cannot create result file: " + finalFile, fileParts);
        }

        for (Path filePart : fileParts) {
            try (OutputStream out = Files.newOutputStream(finalFile, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                Files.copy(filePart, out);
                removeFileParts(Collections.singletonList(filePart));
            } catch (IOException e) {
                return handleException(e, "Error while joining parts of file", fileParts);
            }
        }

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
            removeFileParts(fileParts);
        }

        return false;
    }
}
