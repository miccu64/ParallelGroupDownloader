package server;

import common.StatusEnum;
import common.exceptions.DownloadException;
import common.utils.FilePartUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import static common.utils.PrepareDownloadUtils.serverDownloadPath;

public class FileDownloader implements Callable<StatusEnum> {
    private final ConcurrentLinkedQueue<Path> processedFiles = new ConcurrentLinkedQueue<>();
    private final int blockSize;
    private final long fileSizeInMB;
    private final Path filePath;
    private final String fileName;
    private final URL url;

    public List<Path> getProcessedFiles() {
        return new ArrayList<>(processedFiles);
    }

    public long getFileSizeInMB() {
        return fileSizeInMB;
    }

    public String getFileName() {
        return fileName;
    }

    public FileDownloader(String url, int blockSizeInMB) throws DownloadException {
        try {
            URI uri = new URI(url);
            this.url = uri.toURL();
            fileName = getFileNameFromUrl(uri);
            filePath = Paths.get(String.valueOf(serverDownloadPath), fileName);
        } catch (IllegalArgumentException | MalformedURLException | URISyntaxException e) {
            throw new DownloadException(e, "Malformed URL");
        }

        blockSize = blockSizeInMB * 1024 * 1024;
        fileSizeInMB = findFileSizeInMB();
    }

    @Override
    public StatusEnum call() {
        int blockNumber = 0;
        long transferredCount;

        try (ReadableByteChannel channel = Channels.newChannel(this.url.openStream())) {
            System.out.println("Download started! Url: " + url);

            do {
                Path filePartPath = createFilePartPath(blockNumber);
                File partFile = filePartPath.toFile();
                blockNumber++;

                try (FileOutputStream fileOutputStream = new FileOutputStream(partFile); FileChannel fileOutputChannel = fileOutputStream.getChannel()) {
                    transferredCount = fileOutputChannel.transferFrom(channel, 0, blockSize);
                } catch (SecurityException | IOException e) {
                    return handleDownloadError(e, "Cannot save to file: " + partFile);
                }

                processedFiles.add(filePartPath);
            } while (transferredCount == blockSize);
        } catch (IOException e) {
            return handleDownloadError(e, "Cannot open given URL. Download aborted");
        }

        return StatusEnum.Success;
    }

    private StatusEnum handleDownloadError(Exception e, String message) {
        System.out.println(message);
        e.printStackTrace(System.out);

        FilePartUtils.removeFiles(new ArrayList<>(processedFiles));
        return StatusEnum.Error;
    }

    private String getFileNameFromUrl(URI uri) {
        Path path;
        try {
            path = Paths.get(uri.getPath());
        } catch (Exception ignored) {
            path = Paths.get(uri);
        }

        return path.getFileName().toString();
    }

    private long findFileSizeInMB() {
        long fileSize = -1;

        try {
            URLConnection urlConnection = url.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                httpURLConnection.setRequestMethod("HEAD");
                fileSize = httpURLConnection.getContentLengthLong();
            } else {
                fileSize = urlConnection.getContentLengthLong();
            }

            if (fileSize > 0) {
                fileSize = (long) Math.ceil((double) fileSize / (double) (1024 * 1024));
            }
        } catch (IOException ignored) {
            System.out.println("Could not determine file size");
        }

        return fileSize;
    }

    private Path createFilePartPath(int partNumber) {
        return Paths.get(filePath + ".part" + partNumber).toAbsolutePath();
    }
}
