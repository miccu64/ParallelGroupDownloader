package server;

import common.exceptions.DownloadException;
import common.models.StatusEnum;
import common.utils.FilePartUtils;
import common.utils.VariousUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.atomic.AtomicInteger;

public class FileDownloader implements Callable<StatusEnum> {
    private final ConcurrentLinkedQueue<Path> processedFiles = new ConcurrentLinkedQueue<>();
    private final AtomicInteger udpcastProcessedParts = new AtomicInteger(-1);
    private final long blockSizeInBytes;
    private final int fileSizeInMB;
    private final String fileName;
    private final URL url;
    private final String downloadDirectory;

    public void incrementUdpcastProcessedParts() {
        udpcastProcessedParts.incrementAndGet();
    }

    public List<Path> getProcessedFiles() {
        return new ArrayList<>(processedFiles);
    }

    public int getFileSizeInMB() {
        return fileSizeInMB;
    }

    public String getFileName() {
        return fileName;
    }

    public URL getUrl() {
        return url;
    }

    public int getBlockSizeInMB() {
        return FilePartUtils.bytesToMegabytes(blockSizeInBytes);
    }

    public FileDownloader(String urlString, String downloadDirectory, String fileName, int blockSizeInMB) throws DownloadException {
        if (urlString == null || urlString.isEmpty()) {
            throw new DownloadException("Empty url.");
        }
        if (blockSizeInMB < 1) {
            throw new DownloadException("Block size must be at least equal 1MB.");
        }

        this.downloadDirectory = downloadDirectory;
        try {
            URI uri = parseUrl(urlString);
            this.url = uri.toURL();

            tryCheckIsFile(uri);

            if (fileName == null) {
                fileName = getFileName(uri);
            }
            this.fileName = fileName;
        } catch (IllegalArgumentException | MalformedURLException e) {
            throw new DownloadException(e, "Malformed URL.");
        }

        fileSizeInMB = findFileSizeInMB();
        blockSizeInBytes = FilePartUtils.megabytesToBytes(blockSizeInMB);
    }

    @Override
    public StatusEnum call() {
        int blockNumber = 0;
        long transferredCount;

        try (InputStream inputStream = this.url.openStream();
             ReadableByteChannel channel = Channels.newChannel(inputStream)) {
            System.out.println("Download started! Url: " + url);

            do {
                waitForUdpcastProgress(blockNumber);

                Path filePartPath = FilePartUtils.generateFilePartPath(downloadDirectory, fileName + ".serverpart" + blockNumber, getBlockSizeInMB());
                blockNumber++;

                try (FileOutputStream fileOutputStream = new FileOutputStream(filePartPath.toFile());
                     FileChannel fileOutputChannel = fileOutputStream.getChannel()) {
                    System.out.println("Server downloading: " + filePartPath.getFileName());
                    transferredCount = fileOutputChannel.transferFrom(channel, 0, blockSizeInBytes);
                } catch (SecurityException | IOException e) {
                    processedFiles.add(filePartPath);
                    System.err.println("Cannot save to file: " + filePartPath + ". Error: " + e.getMessage());
                    return StatusEnum.Error;
                }

                if (transferredCount > 0) {
                    processedFiles.add(filePartPath);
                } else {
                    FilePartUtils.removeFile(filePartPath);
                }
            } while (transferredCount == blockSizeInBytes);
        } catch (IOException e) {
            System.err.println("Cannot open given URL. Download aborted. Error: " + e.getMessage());
            return StatusEnum.Error;
        }

        return StatusEnum.Success;
    }

    private URI parseUrl(String urlString) {
        try {
            return new URL(urlString).toURI();
        } catch (Exception e) {
            return Paths.get(urlString).toUri();
        }
    }

    private void tryCheckIsFile(URI uri) throws DownloadException {
        if (url.getProtocol().equals("file")) {
            File file = Paths.get(uri).toFile();
            if (!file.exists()) {
                throw new DownloadException("File at given path does not exist.");
            }
            if (!file.isFile()) {
                throw new DownloadException("Given path does not point to file.");
            }
        }
    }

    private String getFileName(URI uri) {
        Path path;
        try {
            path = Paths.get(uri.getPath());
        } catch (Exception ignored) {
            path = Paths.get(uri);
        }

        return path.getFileName().toString();
    }

    private int findFileSizeInMB() {
        URLConnection urlConnection = null;
        try {
            urlConnection = url.openConnection();
            long fileSizeInBytes;
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                httpURLConnection.setRequestMethod("HEAD");
                fileSizeInBytes = httpURLConnection.getContentLengthLong();
            } else {
                fileSizeInBytes = urlConnection.getContentLengthLong();
            }

            return FilePartUtils.bytesToMegabytes(fileSizeInBytes);
        } catch (IOException ignored) {
            System.out.println("Could not determine file size.");
            return 0;
        } finally {
            if (urlConnection != null) {
                if (urlConnection instanceof HttpURLConnection) {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                    httpURLConnection.disconnect();
                }
                try {
                    urlConnection.getInputStream().close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void waitForUdpcastProgress(int blockNumber) {
        while (udpcastProcessedParts.get() != -1 && (blockNumber - udpcastProcessedParts.get() >= 2)) {
            VariousUtils.sleep(1);
        }
    }
}
