package common.services;

import common.exceptions.DownloadException;
import common.utils.FilePartUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;

import static common.utils.FilePartUtils.removeFile;
import static java.nio.file.StandardOpenOption.*;

public class FileService {
    private final ConcurrentLinkedQueue<Future<String>> checksumFutures = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Future<Boolean>> joinResultFutures = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Path finalFilePath;

    public FileService(Path finalFilePath) throws DownloadException {
        this.finalFilePath = finalFilePath;

        FilePartUtils.removeFile(finalFilePath);
        try {
            Files.createFile(finalFilePath);
        } catch (IOException e) {
            throw new DownloadException(e, "Cannot create file: " + finalFilePath);
        }
    }

    public void addFileToProcess(Path path) {
        checksumFutures.add(executorService.submit(() -> fileChecksum(path)));
    }

    public List<String> waitForChecksums() throws DownloadException {
        List<String> checksums = new ArrayList<>();
        for (Future<String> future : checksumFutures) {
            try {
                checksums.add(future.get(1, TimeUnit.HOURS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new DownloadException("Could not calculate checksums.");
            }
        }
        return checksums;
    }

    public void waitForFilesJoin() throws DownloadException {
        System.out.println("Waiting for end of files joining...");
        waitForChecksums();

        for (Future<Boolean> future : joinResultFutures) {
            try {
                if (!future.get(2, TimeUnit.HOURS)) {
                    throw new DownloadException("Could not join files.");
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new DownloadException("Could not join files.");
            }
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            executorService.shutdownNow();
        }
    }

    private String fileChecksum(Path filePath) throws DownloadException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (InputStream inputStream = Files.newInputStream(filePath);
                 DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest)) {
                byte[] buffer = new byte[32768];
                int readCount = 0;
                while (readCount != -1) {
                    readCount = digestInputStream.read(buffer);
                }
            }
            byte[] checksum = messageDigest.digest();
            String result = Base64.getEncoder().encodeToString(checksum);

            joinResultFutures.add(executorService.submit(() -> mergeWithMainFileAndRemovePart(filePath)));

            return result;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new DownloadException(e, "Could not create file checksum.");
        }
    }

    private boolean mergeWithMainFileAndRemovePart(Path filePart) {
        try (FileChannel out = FileChannel.open(finalFilePath, WRITE, APPEND)) {
            System.out.println("Joining file part: " + filePart.getFileName());

            try (FileChannel in = FileChannel.open(filePart, READ)) {
                long fileSizeInBytes = in.size();
                for (long position = 0; position < fileSizeInBytes; ) {
                    position += in.transferTo(position, fileSizeInBytes - position, out);
                }
            }
            removeFile(filePart);
        } catch (IOException e) {
            System.err.println("Error while joining parts of file. Error: " + e.getMessage());
            return false;
        }
        return true;
    }
}
