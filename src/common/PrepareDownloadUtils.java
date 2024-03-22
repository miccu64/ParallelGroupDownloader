package common;

import java.io.IOException;
import java.io.InputStream;
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

    public static void initProgram() throws DownloaderException {
        createDownloadDirectory();
    }

    public static boolean beforeDownloadCheck(long fileSizeInBytes) throws DownloaderException {
        return checkFreeSpace(fileSizeInBytes);
    }

    public static void checkIsValidUrl(String urlToCheck) throws DownloaderException {
        try {
            URL url = new URL(urlToCheck);
            url.toURI();
            try (InputStream inputStream = url.openStream();
                 ReadableByteChannel ignored = Channels.newChannel(inputStream)){}
        } catch (Exception e) {
            throw new DownloaderException(e, "Malformed URL");
        }
    }

    private static boolean checkFreeSpace(long fileSizeInBytes) throws DownloaderException {
        if (fileSizeInBytes < 1) {
            return true;
        }

        try {
            URI location = PrepareDownloadUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            FileStore store = Files.getFileStore(Paths.get(location));
            return store.getUsableSpace() > fileSizeInBytes * 2;
        } catch (IOException | URISyntaxException e) {
            throw new DownloaderException(e, "Cannot check free space");
        }
    }

    private static void createDownloadDirectory() throws DownloaderException {
        try {
            Files.createDirectories(downloadPath);
        } catch (IOException e) {
            throw new DownloaderException(e, "Could not create download directory");
        }
    }
}
