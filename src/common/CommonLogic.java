package common;

import common.exceptions.DownloadException;
import common.models.StatusEnum;
import common.services.FileService;
import common.services.UdpcastService;
import common.utils.FilePartUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class CommonLogic {
    protected final UdpcastService udpcastService;
    protected final String downloadDirectory;

    protected FileService fileService;

    protected CommonLogic(UdpcastService udpcastService, String downloadDirectory) throws DownloadException {
        this.udpcastService = udpcastService;
        if (downloadDirectory == null) {
            this.downloadDirectory = "";
        } else {
            this.downloadDirectory = downloadDirectory;
        }

        Path path = Paths.get(this.downloadDirectory);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new DownloadException(e, "Could not create download directories.");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    public abstract StatusEnum doWork();

    protected void checkFreeSpace(int summarySizeInMB, int partSizeInMB) throws DownloadException {
        try {
            URI location = CommonLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            FileStore store = Files.getFileStore(Paths.get(location));
            if (store.getUsableSpace() <= (FilePartUtils.megabytesToBytes(summarySizeInMB + partSizeInMB))) {
                throw new DownloadException("Not enough free space.");
            }
        } catch (IOException | URISyntaxException ignored) {
        }
    }

    protected void cleanup() {
        udpcastService.stopUdpcast();

        if (fileService != null) {
            fileService.shutdown();
        }
    }

    protected void renameFile(Path filePath, String newName) {
        Path parent = filePath.getParent();
        Path newFilePath;
        boolean result;
        do {
            if (parent == null) {
                newFilePath = Paths.get(newName);
            } else {
                newFilePath = Paths.get(parent.toString(), newName);
            }
            FilePartUtils.removeFile(newFilePath);

            result = filePath.toFile().renameTo(newFilePath.toFile());
            newName = "1" + newName;
        } while (!result);
    }
}
