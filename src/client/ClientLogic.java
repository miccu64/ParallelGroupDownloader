package client;

import common.CommonLogic;
import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;
import common.infos.EndInfoFile;
import common.infos.StartInfoFile;
import common.models.StatusEnum;
import common.models.UdpcastConfiguration;
import common.services.FileService;
import common.utils.FilePartUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ClientLogic extends CommonLogic {
    private String fileName;

    public ClientLogic(UdpcastConfiguration configuration) throws DownloadException {
        super(new ClientUdpcastService(configuration), configuration.getDirectory());

        this.fileName = configuration.getFileName();
    }

    public StatusEnum doWork() {
        System.out.println("Acting as client. Waiting for server...");

        StatusEnum result;
        try {
            String fileNameFromServer = processStartFile();
            if (this.fileName == null) {
                fileName = fileNameFromServer;
            }
            Path finalFileTempPath = Paths.get(this.downloadDirectory, fileName + ".client");
            finalFileTempPath.toFile().deleteOnExit();

            fileService = new FileService(finalFileTempPath);

            EndInfoFile endInfoFile = null;
            int partCount = 0;
            while (endInfoFile == null) {
                Path filePart = generateFilePartPath(fileName, partCount);
                udpcastService.processFile(filePart);
                partCount++;

                endInfoFile = tryProcessEndFile(filePart);
                if (endInfoFile == null) {
                    fileService.addFileToProcess(filePart);
                }
            }

            compareChecksums(endInfoFile.getChecksums(), fileService.waitForChecksums());
            fileService.waitForFilesJoin();
            renameFile(finalFileTempPath, fileName);

            result = StatusEnum.Success;
        } catch (DownloadException e) {
            result = StatusEnum.Error;
        } finally {
            cleanup();
        }

        return result;
    }

    private String processStartFile() throws DownloadException {
        Path startFilePath = Paths.get(downloadDirectory, "startInfo.txt");
        try {
            udpcastService.processFile(startFilePath);
            StartInfoFile startInfoFile = new StartInfoFile(startFilePath);

            System.out.println("Download started. Url: " + startInfoFile.url + ", file name: " + startInfoFile.fileName);
            if (startInfoFile.summarySizeInMB < 1) {
                System.out.println("Not known file size - program will try download it anyway.");
            } else {
                System.out.println("Expected file size (in MB): " + startInfoFile.summarySizeInMB);
                checkFreeSpace(startInfoFile.summarySizeInMB, startInfoFile.partSizeInMB);
            }

            return startInfoFile.fileName;
        } finally {
            FilePartUtils.removeFile(startFilePath);
        }
    }

    private EndInfoFile tryProcessEndFile(Path filePart) throws DownloadException {
        try {
            EndInfoFile endInfoFile = new EndInfoFile(filePart);
            Path endFilePath = Paths.get(downloadDirectory, "endInfo.txt");
            try {
                Files.move(filePart, endFilePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new DownloadException(e);
            } finally {
                FilePartUtils.removeFile(endFilePath);
            }

            return endInfoFile;
        } catch (InfoFileException ignored) {
            return null;
        }
    }

    private Path generateFilePartPath(String fileName, int partCount) {
        Path path = Paths.get(downloadDirectory, fileName + ".part" + partCount);
        path.toFile().deleteOnExit();
        return path;
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
