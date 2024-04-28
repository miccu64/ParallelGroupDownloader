package server;

import common.DownloadException;
import common.udp.FileInfoHolder;
import common.udp.UdpcastService;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;

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
        FileInfoHolder fileInfoHolder = new FileInfoHolder();
        ArrayList<Path> processedFiles = new ArrayList<>();

        Thread fileDownloaderThread = null;
        try {
            FileDownloader fileDownloader = new FileDownloader(url, 1, fileInfoHolder);
            fileDownloaderThread = new Thread(fileDownloader);
            fileDownloaderThread.start();

            UdpcastService udpcastService = new ServerUdpcastService(9000);

            int partCount = 0;
            Path startInfoFilePath = Paths.get(downloadPath, "startInfo.txt");
            processedFiles.add((startInfoFilePath));
            udpcastService.processFile(startInfoFilePath);

            // TODO: parse file - size check, url print
            String fileName = "todo";

            boolean downloadInProgress = true;
            while (downloadInProgress) {
                Path filePart = createFilePartPath(fileName, partCount);
                processedFiles.add(filePart);
                udpcastService.processFile(filePart);
                // TODO: check if is final part - if yes, end loop, change file name, add to array, check CRCs and join files
            }
        } catch (DownloadException e) {
            if (fileDownloaderThread != null && fileDownloaderThread.isAlive()) {
                fileDownloaderThread.interrupt();
            }

            return 1;
        } finally {
            FilePartUtils.removeFiles(processedFiles);
        }

        return 0;
    }

    private Path createFilePartPath(String fileName, int partCount) {
        return Paths.get(downloadPath, fileName + ".part" + partCount);
    }
}
