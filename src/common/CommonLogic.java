package common;

import common.exceptions.DownloadException;
import common.utils.FilePartUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class CommonLogic {
    protected final String downloadPath;

    protected CommonLogic(Path downloadPath) throws DownloadException {
        this.downloadPath = downloadPath.toString();
        try {
            Files.createDirectories(downloadPath);
        } catch (IOException e) {
            throw new DownloadException(e, "Could not create download directories");
        }
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
}
