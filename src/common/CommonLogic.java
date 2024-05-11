package common;

import common.exceptions.DownloadException;
import common.models.StatusEnum;
import common.services.ChecksumService;
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
    protected final List<Path> processedFiles = new ArrayList<>();
    protected final ChecksumService checksumService = new ChecksumService();
    protected final UdpcastService udpcastService;
    protected final String downloadPath;

    protected CommonLogic(UdpcastService udpcastService, Path downloadPath) throws DownloadException {
        this.udpcastService = udpcastService;
        this.downloadPath = downloadPath.toString();
        try {
            Files.createDirectories(downloadPath);
        } catch (IOException e) {
            throw new DownloadException(e, "Could not create download directories");
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
        checksumService.shutdown();
    }
}
