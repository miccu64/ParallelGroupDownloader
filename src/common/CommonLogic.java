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
import java.util.ArrayList;
import java.util.List;

public abstract class CommonLogic {
    protected final UdpcastService udpcastService;
    protected final String downloadPath;

    protected FileService fileService;

    protected CommonLogic(UdpcastService udpcastService, String downloadPath) throws DownloadException {
        this.udpcastService = udpcastService;
        if (downloadPath == null) {
            this.downloadPath = "";
        } else {
            this.downloadPath = downloadPath;
        }

        Path path = Paths.get(this.downloadPath);
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
}
