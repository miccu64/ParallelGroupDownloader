package common.utils;

import common.exceptions.DownloadException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrepareDownloadUtils {
    public final static Path serverDownloadPath = Paths.get("downloadsServer");
    public final static Path clientDownloadPath = Paths.get("downloadsClient");

    public static void initProgram() throws DownloadException {
        createDownloadDirectories();
    }

    public static void checkFreeSpace(int summarySizeInMB, int partSizeInMB) throws DownloadException {
        try {
            URI location = PrepareDownloadUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            FileStore store = Files.getFileStore(Paths.get(location));
            if (store.getUsableSpace() <= (FilePartUtils.megabytesToBytes(summarySizeInMB + partSizeInMB))) {
                throw new DownloadException("Not enough free space.");
            }
        } catch (IOException | URISyntaxException ignored) {
        }
    }

    private static void createDownloadDirectories() throws DownloadException {
        try {
            Files.createDirectories(serverDownloadPath);
            Files.createDirectories(clientDownloadPath);
        } catch (IOException e) {
            throw new DownloadException(e, "Could not create download directories");
        }
    }
}
