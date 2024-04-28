package client;

import common.DownloadException;
import common.parser.EndFileContent;
import common.udp.UdpcastService;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ClientMainThread implements Callable<Integer> {
    private final String downloadPath = PrepareDownloadUtils.clientDownloadPath.toString();

    @Override
    public Integer call() {
        UdpcastService udpcastService = new ClientUdpcastService(9000);

        int partCount = 0;
        ArrayList<Path> processedFiles = new ArrayList<>();
        try {
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

                try {
                    long fileSizeInBytes = Files.size(filePart);
                    if (fileSizeInBytes < 1000) {
                        EndFileContent endFileContent = new EndFileContent(filePart);

                        // TODO: change file name, add to array, check CRCs and join files
                        downloadInProgress = false;
                    }
                } catch (IOException e) {
                    throw new DownloadException(e);
                }
            }
        } catch (DownloadException e) {
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
