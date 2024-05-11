package common.services;

import common.exceptions.DownloadException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;

public class ChecksumService {
    private final ConcurrentLinkedQueue<Future<String>> futures = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public void addFileToProcess(Path path) {
        futures.add(executorService.submit(() -> fileChecksum(path)));
    }

    public List<String> getChecksums() throws DownloadException {
        List<String> checksums = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                checksums.add(future.get(1, TimeUnit.HOURS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new DownloadException("Could not calculate checksums.");
            }
        }
        return checksums;
    }

    public void shutdown() {
        executorService.shutdownNow();
    }

    private String fileChecksum(Path filePath) throws DownloadException {
        if (filePath.toFile().length() < 1) {
            throw new DownloadException("Empty file.");
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            try (InputStream inputStream = Files.newInputStream(filePath);
                 DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest)) {
                byte[] buffer = new byte[50000000];
                while (digestInputStream.read(buffer) != -1) {
                }
            }
            byte[] checksum = messageDigest.digest();
            return Base64.getEncoder().encodeToString(checksum);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new DownloadException(e, "Could not create file checksum.");
        }
    }
}
