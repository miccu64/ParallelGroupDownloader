package common.utils;

import common.DownloadException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrepareDownloadUtils {
    public final static Path downloadPath = Paths.get("downloads");

    public static void initProgram() throws DownloadException {
        createDownloadDirectory();
    }

    public static boolean beforeDownloadCheck(long fileSizeInBytes) throws DownloadException {
        return checkFreeSpace(fileSizeInBytes);
    }

    public static void checkIsValidUrl(String urlToCheck) throws DownloadException {
        try {
            URL url = new URL(urlToCheck);
            url.toURI();
            try (ReadableByteChannel ignored = Channels.newChannel(url.openStream())){}
        } catch (Exception e) {
            throw new DownloadException(e, "Malformed URL");
        }
    }

    private static boolean checkFreeSpace(long fileSizeInBytes) throws DownloadException {
        if (fileSizeInBytes < 1) {
            return true;
        }

        try {
            URI location = PrepareDownloadUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            FileStore store = Files.getFileStore(Paths.get(location));
            return store.getUsableSpace() > fileSizeInBytes * 2;
        } catch (IOException | URISyntaxException e) {
            throw new DownloadException(e, "Cannot check free space");
        }
    }

    private static void createDownloadDirectory() throws DownloadException {
        try {
            Files.createDirectories(downloadPath);
        } catch (IOException e) {
            throw new DownloadException(e, "Could not create download directory");
        }
    }
}
