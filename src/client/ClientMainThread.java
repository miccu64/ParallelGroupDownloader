package client;

import common.DownloadException;
import common.parser.EndFileContent;
import common.parser.StartFileContent;
import common.udp.UdpcastService;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        UdpcastService udpcastService = new ClientUdpcastService(9000);

        int partCount = 0;

        try {
            processStartFile();
            String fileName = "todo";

            boolean downloadInProgress = true;
            while (downloadInProgress) {
                Path filePart = createFilePartPath(fileName, partCount);
                processedFiles.add(filePart);
                udpcastService.processFile(filePart);

                if (canBeInfoFile(filePart)) {
                    EndFileContent endFileContent = new EndFileContent(filePart);

                    // TODO: change file name, add to array, check CRCs and join files
                    downloadInProgress = false;
                }
            }
        } catch (DownloadException e) {
            return 1;
        } finally {
            FilePartUtils.removeFiles(processedFiles);
        }

        return 0;
    }

    private String processStartFile() throws DownloadException {
        Path startInfoFilePath = Paths.get(downloadPath, "startInfo.txt");
        processedFiles.add((startInfoFilePath));
        udpcastService.processFile(startInfoFilePath);

        if (canBeInfoFile(startInfoFilePath)) {
            StartFileContent startFileContent = new StartFileContent(startInfoFilePath);
            System.out.println("Download started. Url: " + startFileContent.url + ", file name: " + startFileContent.fileName);
            if (startFileContent.fileSizeInMB == 0) {
                System.out.println("Not known file size - program will try download it anyway.");
            } else {
                System.out.println("Expected file size: " + startFileContent.fileSizeInMB);
            }

            if (!PrepareDownloadUtils.checkFreeSpace(startFileContent.fileSizeInMB)) {
                throw new DownloadException("Not enough free space. Exiting...");
            }

            return startFileContent.fileName;
        } else {
            throw new DownloadException("Received file not being config file as first file. Exiting...");
        }
    }

    private Path createFilePartPath(String fileName, int partCount) {
        return Paths.get(downloadPath, fileName + ".part" + partCount);
    }

    private boolean canBeInfoFile(Path file) throws DownloadException {
        try {
            long fileSizeInBytes = Files.size(file);
            int oneMBAsBytes = 1000000;

            return fileSizeInBytes < oneMBAsBytes;
        } catch (IOException e) {
            throw new DownloadException(e);
        }
    }
}
