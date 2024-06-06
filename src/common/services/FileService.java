package common.services;

import common.exceptions.DownloadException;
import common.utils.FilePartUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.zip.Adler32;

import static common.utils.FilePartUtils.removeFile;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.WRITE;

public class FileService {
    private final ConcurrentLinkedQueue<Future<String>> futures = new ConcurrentLinkedQueue<>();
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
        try {
            futures.add(executorService.submit(() -> calcChecksumAndMerge(path)));
        } catch (RejectedExecutionException ignored) {
        }
    }

    public List<String> waitForChecksums() throws DownloadException {
        List<String> checksums = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                checksums.add(future.get(2, TimeUnit.HOURS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new DownloadException("Could not calculate checksums and join files.");
            }
        }

        return checksums;
    }

    public void shutdownNow() {
        executorService.shutdownNow();
    }

    private String calcChecksumAndMerge(Path filePart) throws DownloadException {
        System.out.println("Joining and calculating checksum for file part: " + filePart.getFileName());

        Adler32 adler = new Adler32();
        try (FileChannel out = FileChannel.open(finalFilePath, WRITE, APPEND)) {
            try (InputStream inputStream = Files.newInputStream(filePart)) {
                byte[] buffer = new byte[1024 * 8];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    adler.update(buffer, 0, bytesRead);

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                    while (byteBuffer.hasRemaining()) {
                        int ignored = out.write(byteBuffer);
                    }
                }
            }

            removeFile(filePart);
        } catch (IOException e) {
            String error = e.getMessage() != null ? " Error: " + e.getMessage() : "";
            throw new DownloadException("Error while calculating checksum and joining parts of file." + error);
        }

        System.out.println("Merged file part: " + filePart.getFileName());
        return String.valueOf(adler.getValue());
    }
}
