package client;

import common.services.ChecksumService;
import common.ILogic;
import common.StatusEnum;
import common.services.UdpcastService;
import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;
import common.infos.EndInfoFile;
import common.infos.StartInfoFile;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ClientLogic implements ILogic {
    private final String downloadPath = PrepareDownloadUtils.clientDownloadPath.toString();
    private final List<Path> processedFiles = new ArrayList<>();
    private final ChecksumService checksumService = new ChecksumService();
    private final UdpcastService udpcastService;

    private Path startFilePath;
    private Path endFilePath;

    public ClientLogic(int port, String udpcastPath) throws DownloadException {
        this.udpcastService = new ClientUdpcastService(port, udpcastPath);
    }

    public StatusEnum doWork() {
        StatusEnum result;
        try {
            String fileName = processStartFile();

            EndInfoFile endInfoFile = null;
            int partCount = 0;
            while (endInfoFile == null) {
                Path filePart = generateFilePartPath(fileName, partCount);
                processedFiles.add(filePart);
                udpcastService.processFile(filePart);
                partCount++;

                endInfoFile = tryProcessEndFile(filePart);
                if (endInfoFile == null) {
                    checksumService.addFileToProcess(filePart);
                }

                // TODO: avoid infinite loop
            }

            compareChecksums(endInfoFile.getChecksums(), checksumService.getChecksums());
            FilePartUtils.joinAndRemoveFileParts(processedFiles);

            result = StatusEnum.Success;
        } catch (DownloadException e) {
            System.out.println("Exiting...");
            result = StatusEnum.Error;
        } finally {
            if (startFilePath != null) {
                processedFiles.add(startFilePath);
            }
            if (endFilePath != null) {
                processedFiles.add(endFilePath);
            }
            checksumService.shutdown();

            FilePartUtils.removeFiles(processedFiles);
        }

        return result;
    }

    private String processStartFile() throws DownloadException {
        startFilePath = Paths.get(downloadPath, "startInfo.txt");
        udpcastService.processFile(startFilePath);
        StartInfoFile startInfoFile = new StartInfoFile(startFilePath);

        System.out.println("Download started. Url: " + startInfoFile.url + ", file name: " + startInfoFile.fileName);
        if (startInfoFile.summarySizeInMB < 1) {
            System.out.println("Not known file size - program will try download it anyway.");
        } else {
            System.out.println("Expected file size (in MB): " + startInfoFile.summarySizeInMB);
            PrepareDownloadUtils.checkFreeSpace(startInfoFile.summarySizeInMB, startInfoFile.partSizeInMB);
        }

        return startInfoFile.fileName;
    }

    private EndInfoFile tryProcessEndFile(Path filePart) throws DownloadException {
        try {
            EndInfoFile endInfoFile = new EndInfoFile(filePart);
            endFilePath = Paths.get(downloadPath, "endInfo.txt");
            try {
                Files.move(filePart, endFilePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new DownloadException(e);
            }
            processedFiles.remove(filePart);

            return endInfoFile;
        } catch (InfoFileException ignored) {
            return null;
        }
    }

    private Path generateFilePartPath(String fileName, int partCount) {
        return Paths.get(downloadPath, fileName + ".part" + partCount);
    }

    private void compareChecksums(List<String> expectedChecksums, List<String> actualChecksums) throws DownloadException {
        if (expectedChecksums.size() != actualChecksums.size()) {
            throw new DownloadException("Checksums count does not equals downloaded files count.");
        }

        for (int i = 0; i < expectedChecksums.size(); i++) {
            String expected = expectedChecksums.get(i);
            String actual = actualChecksums.get(i);
            if (!expected.equals(actual)) {
                throw new DownloadException("Checksum mismatch.");
            }
        }
    }
}
