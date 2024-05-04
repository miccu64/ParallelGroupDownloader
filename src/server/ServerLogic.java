package server;

import common.services.ChecksumService;
import common.ILogic;
import common.StatusEnum;
import common.services.UdpcastService;
import common.exceptions.DownloadException;
import common.infos.EndInfoFile;
import common.infos.StartInfoFile;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ServerLogic implements ILogic {
    private final String downloadPath = PrepareDownloadUtils.serverDownloadPath.toString();
    private final String url;
    private final UdpcastService udpcastService;
    private final ArrayList<Path> processedFiles = new ArrayList<>();
    private final ChecksumService checksumService = new ChecksumService();

    private int processedPartsCount = 0;
    private Path startFilePath;
    private Path endFilePath;

    public ServerLogic(String url, int port, String udpcastPath) throws DownloadException {
        this.url = url;
        udpcastService = new ServerUdpcastService(port, udpcastPath);
    }

    public StatusEnum doWork() {
        StatusEnum result;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<StatusEnum> fileDownloaderFuture;
        try {
            int partSizeInMB = 1;
            FileDownloader fileDownloader = new FileDownloader(url, partSizeInMB);
            PrepareDownloadUtils.checkFreeSpace(fileDownloader.getFileSizeInMB(), partSizeInMB);

            fileDownloaderFuture = executorService.submit(fileDownloader);
            checkDownloadIsProperlyStarted(fileDownloaderFuture);

            processStartFile(fileDownloader.getFileName(), fileDownloader.getFileSizeInMB(), partSizeInMB);

            do {
                Path fileToProcess = tryGetUnprocessedFile(fileDownloader.getProcessedFiles());
                if (fileToProcess != null) {
                    processFilePart(fileToProcess);
                }
            } while (!fileDownloaderFuture.isDone());

            checkFileDownloaderSuccess(fileDownloaderFuture);
            processRemainingParts(fileDownloader.getProcessedFiles());

            processEndFile();

            FilePartUtils.joinAndRemoveFileParts(processedFiles);

            result = StatusEnum.Success;
        } catch (DownloadException e) {
            checksumService.shutdown();

            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException ignored) {
                executorService.shutdownNow();
            }

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

    private void checkDownloadIsProperlyStarted(Future<StatusEnum> fileDownloaderFuture) throws DownloadException {
        sleepOneSecond();
        if (fileDownloaderFuture.isDone()) {
            checkFileDownloaderSuccess(fileDownloaderFuture);
        }
    }

    private void processStartFile(String fileName, int fileSizeInMB, int partSizeInMB) throws DownloadException {
        StartInfoFile startInfoFile = new StartInfoFile(downloadPath, url, fileName, fileSizeInMB, partSizeInMB);
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

    private void processFilePart(Path filePath) throws DownloadException {
        processedFiles.add(filePath);

        udpcastService.processFile(filePath);
        checksumService.addFileToProcess(filePath);

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
        List<String> checksums = checksumService.getChecksums();
        EndInfoFile endInfoFile = new EndInfoFile(downloadPath, checksums);
        endFilePath = endInfoFile.filePath;
        udpcastService.processFile(endFilePath);
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
