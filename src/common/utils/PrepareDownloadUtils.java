package common.utils;

import common.exceptions.DownloadException;

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
    public final static Path serverDownloadPath = Paths.get("downloadsServer");
    public final static Path clientDownloadPath = Paths.get("downloadsClient");

    public static void initProgram() throws DownloadException {
        //checkUdpcastPacketPresence();
        createDownloadDirectories();
    }

    public static boolean beforeDownloadCheck(long fileSizeInBytes) throws DownloadException {
        return checkFreeSpace(fileSizeInBytes);
    }

    public static void checkIsValidUrl(String urlToCheck) throws DownloadException {
        try {
            URL url = new URL(urlToCheck);
            url.toURI();
            try (ReadableByteChannel ignored = Channels.newChannel(url.openStream())) {
            }
        } catch (Exception e) {
            throw new DownloadException(e, "Malformed URL");
        }
    }

    private static void checkUdpcastPacketPresence() throws DownloadException {
        try {
            // TODO: "/bin/bash", "-c" - should be there?
            new ProcessBuilder()
                    .command("udp-receiver", "--license")
                    .start()
                    .destroy();
            new ProcessBuilder()
                    .command("udp-sender", "--license")
                    .start()
                    .destroy();
        } catch (IOException e) {
            throw new DownloadException(e, "Udpcast packet is not present. Please install it via terminal: 'sudo apt install udpcast'.");
        }
    }

    public static boolean checkFreeSpace(long fileSizeInMB) throws DownloadException {
        if (fileSizeInMB < 1) {
            return true;
        }

        try {
            URI location = PrepareDownloadUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            FileStore store = Files.getFileStore(Paths.get(location));
            // TODO: should be fileSize + single partSize
            return store.getUsableSpace() > fileSizeInMB * 1000;
        } catch (IOException | URISyntaxException e) {
            throw new DownloadException(e, "Cannot check free space");
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
