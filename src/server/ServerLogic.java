package server;

import common.CommonLogic;
import common.exceptions.DownloadException;
import common.infos.EndInfoFile;
import common.infos.StartInfoFile;
import common.models.StatusEnum;
import common.models.UdpcastConfiguration;
import common.services.FileService;
import common.utils.FilePartUtils;
import common.utils.VariousUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerLogic extends CommonLogic {
    private final int delayInMinutes;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final FileDownloader fileDownloader;

    private int processedPartsCount = 0;
    private final AtomicBoolean invokedCleanup = new AtomicBoolean(false);

    public ServerLogic(UdpcastConfiguration configuration) throws DownloadException {
        super(new ServerUdpcastService(configuration), configuration.getDirectory());

        delayInMinutes = configuration.getDelayMinutes();
        fileDownloader = new FileDownloader(configuration.getUrl(), downloadDirectory, configuration.getFileName(), configuration.getBlockSizeInMb());
    }

    public StatusEnum doWork() {
        System.out.println("Acting as server.");
        fileDownloader.incrementUdpcastProcessedParts();

        Path finalFileTempPath = Paths.get(this.downloadDirectory, fileDownloader.getFileName() + ".server");
        FilePartUtils.markToDeleteOnExit(finalFileTempPath);

        StatusEnum result;
        try {
            int sizeInMBWithMargin = fileDownloader.getFileSizeInMB() + fileDownloader.getBlockSizeInMB();
            if (!FilePartUtils.checkFreeSpace(Paths.get(this.downloadDirectory), sizeInMBWithMargin)) {
                throw new DownloadException("Not enough free space.");
            }

            Future<StatusEnum> fileDownloaderFuture = executorService.submit(fileDownloader);
            checkDownloadIsProperlyStarted(fileDownloaderFuture);

            delayIfRequested();
            fileService = new FileService(finalFileTempPath);

            boolean processedStartFile;
            do {
                processedStartFile = processStartFile();
                if (!processedStartFile) {
                    System.out.println("Could not find any clients. Program will retry in next 1 minute if download will be still in progress.");
                    VariousUtils.sleep(60);
                }
            } while (!processedStartFile && !fileDownloaderFuture.isDone());

            if (processedStartFile) {
                do {
                    Path fileToProcess = tryGetUnprocessedFile();
                    if (fileToProcess != null) {
                        processFilePart(fileToProcess);
                    } else {
                        VariousUtils.sleep(1);
                    }
                } while (!fileDownloaderFuture.isDone());

                processRemainingParts(fileDownloaderFuture);

                processEndFile();
            } else {
                Path fileToProcess;
                while ((fileToProcess = tryGetUnprocessedFile()) != null) {
                    fileService.addFileToProcess(fileToProcess);
                }
            }

            Path finalFile = renameFile(finalFileTempPath, fileDownloader.getFileName());

            System.out.println("Success! Downloaded file: " + finalFile);
            udpcastService.printStatsOnSuccess();
            result = StatusEnum.Success;
        } catch (DownloadException e) {
            result = StatusEnum.Error;
            cleanup();
        }

        return result;
    }

    @Override
    protected void cleanup() {
        if (invokedCleanup.getAndSet(true)) {
            return;
        }

        VariousUtils.suppressStdOut();
        VariousUtils.suppressStdErr();

        executorService.shutdownNow();

        if (fileService != null) {
            fileService.shutdownNow();
        }

        ((ServerUdpcastService) udpcastService).shutdownClients();

        super.cleanup();
    }

    private void checkDownloadIsProperlyStarted(Future<StatusEnum> fileDownloaderFuture) throws DownloadException {
        VariousUtils.sleep(1);
        if (fileDownloaderFuture.isDone()) {
            checkFileDownloaderSuccess(fileDownloaderFuture);
        }
    }

    private void delayIfRequested() {
        if (delayInMinutes > 0) {
            System.out.println("Starting delay for minutes: " + delayInMinutes);
            VariousUtils.sleep(delayInMinutes * 60);
            System.out.println("Delay end.");
        }
    }

    private boolean processStartFile() throws DownloadException {
        String url = fileDownloader.getUrl().toString();
        String fileName = fileDownloader.getFileName();
        int fileSizeInMB = fileDownloader.getFileSizeInMB();
        int blockSizeInMB = fileDownloader.getBlockSizeInMB();

        StartInfoFile startInfoFile = new StartInfoFile(downloadDirectory, url, fileName, fileSizeInMB, blockSizeInMB);
        long startTime = System.nanoTime();
        try {
            udpcastService.processFile(startInfoFile.filePath);
            udpcastService.setDownloadSize(startInfoFile.summarySizeInMB);
            return true;
        } catch (DownloadException e) {
            long executionTime = System.nanoTime() - startTime;
            boolean isError = TimeUnit.NANOSECONDS.toMillis(executionTime) < 1000;
            if (isError) {
                throw e;
            }
            return false;
        } finally {
            FilePartUtils.removeFile(startInfoFile.filePath);
        }
    }

    private Path tryGetUnprocessedFile() {
        try {
            Path path = fileDownloader.getProcessedFiles().get(processedPartsCount);
            processedPartsCount++;
            return path;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void processFilePart(Path filePath) throws DownloadException {
        udpcastService.processFile(filePath);
        fileService.addFileToProcess(filePath);

        fileDownloader.incrementUdpcastProcessedParts();
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
        EndInfoFile endInfoFile = new EndInfoFile(downloadDirectory, fileService.waitForChecksums());
        try {
            udpcastService.processFile(endInfoFile.filePath);
        } finally {
            FilePartUtils.removeFile(endInfoFile.filePath);
        }
    }
}
