package common;

import common.exceptions.DownloadException;
import common.models.StatusEnum;
import common.services.FileService;
import common.services.UdpcastService;
import common.utils.FilePartUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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

    protected void cleanup() {
        suppressStdErr();

        udpcastService.stopUdpcast();

        if (fileService != null) {
            fileService.shutdownNow();
        }
    }

    protected Path renameFile(Path filePath, String newName) {
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
            if (!result) {
                newName = "1" + newName;
            }
        } while (!result);

        return Paths.get(String.valueOf(parent), newName).toAbsolutePath();
    }

    private void suppressStdErr() {
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        });
        System.setErr(dummyStream);
    }
}
