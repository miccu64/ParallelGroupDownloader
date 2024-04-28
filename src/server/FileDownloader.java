package server;

import common.DownloadException;
import common.udp.FileInfoHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static common.utils.PrepareDownloadUtils.serverDownloadPath;

public class FileDownloader implements Runnable {
    private final int blockSize;
    private final long fileSizeInMB;
    private final FileInfoHolder fileInfoHolder;
    private final Path filePath;
    private final String fileName;
    private final URL url;

    public long getFileSizeInMB() {
        return fileSizeInMB;
    }
    public String getFileName() {
        return fileName;
    }

    public FileDownloader(String url, int blockSizeInMB, FileInfoHolder fileInfoHolder) throws DownloadException {
        this.fileInfoHolder = fileInfoHolder;

        try {
            URI uri = new URI(url);
            this.url = uri.toURL();
            fileName = getFileNameFromUrl(uri);
            filePath = Paths.get(String.valueOf(serverDownloadPath), fileName);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new DownloadException(e, "Malformed URL");
        }

        blockSize = blockSizeInMB * 1024 * 1024;
        fileSizeInMB = findFileSizeInMB();
    }

    @Override
    public void run() {
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
                    handleDownloadError(e, "Cannot save to file: " + partFile);
                    return;
                }

                fileInfoHolder.filesToProcess.add(filePartPath);
            } while (transferredCount == blockSize);
        } catch (IOException e) {
            handleDownloadError(e, "Cannot open given URL. Download aborted");
        }
    }

    private void handleDownloadError(Exception e, String message) {
        System.out.println(message);
        e.printStackTrace(System.out);

        // TODO: clean downloaded files
        fileInfoHolder.setErrorStatus();
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
