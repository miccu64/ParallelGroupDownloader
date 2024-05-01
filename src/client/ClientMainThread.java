package client;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;
import common.parser.EndInfoFile;
import common.parser.StartInfoFile;
import common.udp.UdpcastService;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ClientMainThread implements Callable<Integer> {
    private final String downloadPath = PrepareDownloadUtils.clientDownloadPath.toString();
    private final List<Path> processedFiles = new ArrayList<>();
    private final UdpcastService udpcastService;

    public ClientMainThread(UdpcastService udpcastService) {
        this.udpcastService = udpcastService;
    }

    @Override
    public Integer call() {
        int result = 1;
        UdpcastService udpcastService = new ClientUdpcastService(9000);
        try {
            String fileName = processStartFile();

            int partCount = 0;
            boolean gotEndInfo = false;
            while (!gotEndInfo) {
                Path filePart = createFilePartPath(fileName, partCount);
                processedFiles.add(filePart);
                udpcastService.processFile(filePart);
                partCount++;

                // TODO: avoid infinite loop
                gotEndInfo = tryProcessEndFile(filePart);
            }
            result = 0;
        } catch (DownloadException e) {
            System.out.println("Exiting...");
        } finally {
            FilePartUtils.removeFiles(processedFiles);
        }

        return result;
    }

    private String processStartFile() throws DownloadException {
        Path startInfoFilePath = Paths.get(downloadPath, "startInfo.txt");
        processedFiles.add((startInfoFilePath));
        udpcastService.processFile(startInfoFilePath);

        StartInfoFile startInfoFile = new StartInfoFile(startInfoFilePath);
        System.out.println("Download started. Url: " + startInfoFile.url + ", file name: " + startInfoFile.fileName);
        if (startInfoFile.fileSizeInMB == 0) {
            System.out.println("Not known file size - program will try download it anyway.");
        } else {
            System.out.println("Expected file size: " + startInfoFile.fileSizeInMB);
            if (!PrepareDownloadUtils.checkFreeSpace(startInfoFile.fileSizeInMB)) {
                throw new DownloadException("Not enough free space. Exiting...");
            }
        }

        return startInfoFile.fileName;
    }

    private boolean tryProcessEndFile(Path filePart) throws DownloadException {
        try {
            EndInfoFile endInfoFile = new EndInfoFile(filePart);
            Path endFilePath = Paths.get(downloadPath, "endInfo.txt");
            processedFiles.add(endFilePath);
            try {
                Files.move(filePart, endFilePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new DownloadException(e);
            }
            processedFiles.remove(filePart);

            // TODO: check CRCs and join files
            return true;
        } catch (InfoFileException ignored) {
            return false;
        }
    }

    private Path createFilePartPath(String fileName, int partCount) {
        return Paths.get(downloadPath, fileName + ".part" + partCount);
    }
}
