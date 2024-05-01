package server;

import common.StatusEnum;
import common.exceptions.DownloadException;
import common.parser.EndInfoFile;
import common.parser.StartInfoFile;
import common.udp.UdpcastService;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServerMainThread {
    private final String downloadPath = PrepareDownloadUtils.serverDownloadPath.toString();
    private final String url;
    private final UdpcastService udpcastService;
    private final ArrayList<Path> processedFiles;

    private int processedPartsCount = 0;
    private Path startFilePath;
    private Path endFilePath;

    public ServerMainThread(String url) {
        this.url = url;
        udpcastService = new ServerUdpcastService(9000);
        processedFiles = new ArrayList<>();
    }

    public StatusEnum doWork() {
        StatusEnum result;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<StatusEnum> fileDownloaderFuture = null;
        try {
            FileDownloader fileDownloader = new FileDownloader(url, 1);
            fileDownloaderFuture = executorService.submit(fileDownloader);

            processStartFile(fileDownloader.getFileName(), fileDownloader.getFileSizeInMB());

            do {
                Path fileToProcess = tryGetUnprocessedFile(fileDownloader.getProcessedFiles());
                if (fileToProcess == null) {
                    sleep();
                } else {
                    processFilePart(fileToProcess);
                }
            } while (!fileDownloaderFuture.isDone());

            checkFileDownloaderSuccess(fileDownloaderFuture);
            processRemainingParts(fileDownloader.getProcessedFiles());
            processEndFile();

            FilePartUtils.joinAndDeleteFileParts(processedFiles);

            result = StatusEnum.Success;
        } catch (DownloadException e) {
            if (fileDownloaderFuture != null && !fileDownloaderFuture.isDone()) {
                fileDownloaderFuture.cancel(true);
            }
            executorService.shutdown();

            result = StatusEnum.Error;
        } finally {
            if (startFilePath != null) {
                processedFiles.add(startFilePath);
            }
            if (endFilePath != null) {
                processedFiles.add(endFilePath);
            }
            FilePartUtils.removeFiles(processedFiles);
        }

        return result;
    }

    private void processStartFile(String fileName, long fileSizeInMB) throws DownloadException {
        StartInfoFile startInfoFile = new StartInfoFile(downloadPath, url, fileName, fileSizeInMB);
        startFilePath = startInfoFile.filePath;
        udpcastService.processFile(startFilePath);
    }

    private Path tryGetUnprocessedFile(List<Path> fileDownloaderProcessedFiles) {
        try {
            return fileDownloaderProcessedFiles.get(processedPartsCount);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processFilePart(Path filePath) throws DownloadException {
        processedFiles.add(filePath);
        udpcastService.processFile(filePath);
        processedPartsCount++;
    }

    private void checkFileDownloaderSuccess(Future<StatusEnum> fileDownloaderFuture) throws DownloadException {
        try {
            StatusEnum fileDownloaderStatus = fileDownloaderFuture.get();
            if (fileDownloaderStatus != StatusEnum.Success) {
                throw new DownloadException("File downloader error.");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new DownloadException(e);
        }
    }

    private void processRemainingParts(List<Path> fileDownloaderProcessedFiles) throws DownloadException {
        Path fileToProcess;
        while ((fileToProcess = tryGetUnprocessedFile(fileDownloaderProcessedFiles)) != null) {
            processFilePart(fileToProcess);
        }
    }

    private void processEndFile() throws DownloadException {
        EndInfoFile endInfoFile = new EndInfoFile(downloadPath, processedFiles);
        endFilePath = endInfoFile.filePath;
        udpcastService.processFile(endFilePath);
    }
}
