package server;

import common.DownloadException;
import common.parser.StartFileContent;
import common.udp.FileInfoHolder;
import common.udp.UdpcastService;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ServerMainThread implements Callable<Integer> {
    private final String downloadPath = PrepareDownloadUtils.serverDownloadPath.toString();
    private final String url;

    public ServerMainThread(String url) {
        this.url = url;
    }

    @Override
    public Integer call() {
        int result;
        FileInfoHolder fileInfoHolder = new FileInfoHolder();
        ArrayList<Path> processedFiles = new ArrayList<>();

        Thread fileDownloaderThread = null;
        try {
            FileDownloader fileDownloader = new FileDownloader(url, 1, fileInfoHolder);
            fileDownloaderThread = new Thread(fileDownloader);
            fileDownloaderThread.start();

            Path startInfoFilePath = Paths.get(downloadPath, "startInfo.txt");
            processedFiles.add(startInfoFilePath);
            StartFileContent startInfoFileContent = new StartFileContent(url, fileDownloader.getFileName(), fileDownloader.getFileSizeInMB());
            try {
                Files.write(startInfoFilePath, startInfoFileContent.toString().getBytes());
            } catch (IOException e) {
                throw new DownloadException(e, "Could not save file: " + startInfoFilePath);
            }

            UdpcastService udpcastService = new ServerUdpcastService(9000);
            udpcastService.processFile(startInfoFilePath);

            boolean downloadInProgress = true;
            while (fileInfoHolder.isInProgress()) {
                // TODO: check if is final part - if yes, end loop, change file name, add to array, check CRCs and join files
            }

            if (fileInfoHolder.isSuccess()) {
                result = 0;
            } else {
                result = 1;
            }
        } catch (DownloadException e) {
            if (fileDownloaderThread != null && fileDownloaderThread.isAlive()) {
                fileDownloaderThread.interrupt();
            }

            result = 1;
        } finally {
            FilePartUtils.removeFiles(processedFiles);
        }

        return result;
    }

    private Path createFilePartPath(String fileName, int partCount) {
        return Paths.get(downloadPath, fileName + ".part" + partCount);
    }
}
