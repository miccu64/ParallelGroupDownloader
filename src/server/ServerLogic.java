package server;

import common.CommonLogic;
import common.StatusEnum;
import common.exceptions.DownloadException;
import common.infos.EndInfoFile;
import common.infos.StartInfoFile;
import common.utils.FilePartUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

public class ServerLogic extends CommonLogic {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final FileDownloader fileDownloader;

    private int processedPartsCount = 0;

    public ServerLogic(String url, int port, String udpcastPath, int partSizeInMB) throws DownloadException {
        super(new ServerUdpcastService(port, udpcastPath), Paths.get("downloadsServer"));

        fileDownloader = new FileDownloader(url, partSizeInMB, downloadPath);
    }

    public StatusEnum doWork() {
        StatusEnum result;
        try {
            checkFreeSpace(fileDownloader.getFileSizeInMB(), fileDownloader.getBlockSizeInMB());

            Future<StatusEnum> fileDownloaderFuture = executorService.submit(fileDownloader);
            checkDownloadIsProperlyStarted(fileDownloaderFuture);

            processStartFile();

            do {
                Path fileToProcess = tryGetUnprocessedFile();
                if (fileToProcess != null) {
                    processFilePart(fileToProcess);
                }
            } while (!fileDownloaderFuture.isDone());

            processRemainingParts(fileDownloaderFuture);

            processEndFile();
            FilePartUtils.joinAndRemoveFileParts(processedFiles);

            result = StatusEnum.Success;
        } catch (DownloadException e) {
            result = StatusEnum.Error;
        } finally {
            cleanup();
        }

        return result;
    }

    @Override
    protected void cleanup() {
        executorService.shutdown();

        super.cleanup();

        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            executorService.shutdownNow();
        }
    }

    private void checkDownloadIsProperlyStarted(Future<StatusEnum> fileDownloaderFuture) throws DownloadException {
        sleepOneSecond();
        if (fileDownloaderFuture.isDone()) {
            checkFileDownloaderSuccess(fileDownloaderFuture);
        }
    }

    private void processStartFile() throws DownloadException {
        String url = fileDownloader.getUrl().toString();
        String fileName = fileDownloader.getFileName();
        int fileSizeInMB = fileDownloader.getFileSizeInMB();
        int blockSizeInMB = fileDownloader.getBlockSizeInMB();

        StartInfoFile startInfoFile = new StartInfoFile(downloadPath, url, fileName, fileSizeInMB, blockSizeInMB);
        try {
            udpcastService.processFile(startInfoFile.filePath);
        } finally {
            FilePartUtils.removeFile(startInfoFile.filePath);
        }
    }

    private Path tryGetUnprocessedFile() {
        try {
            return fileDownloader.getProcessedFiles().get(processedPartsCount);
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

    private void processRemainingParts(Future<StatusEnum> fileDownloaderFuture) throws DownloadException {
        checkFileDownloaderSuccess(fileDownloaderFuture);

        Path fileToProcess;
        while ((fileToProcess = tryGetUnprocessedFile()) != null) {
            processFilePart(fileToProcess);
        }
    }

    private void processEndFile() throws DownloadException {
        EndInfoFile endInfoFile = new EndInfoFile(downloadPath, checksumService.getChecksums());
        try {
            udpcastService.processFile(endInfoFile.filePath);
        } finally {
            FilePartUtils.removeFile(endInfoFile.filePath);
        }
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
