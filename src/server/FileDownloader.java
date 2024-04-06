package server;

import common.DownloadException;
import common.udp.FileInfoHolder;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import static common.utils.FilePartUtils.removeFileParts;
import static common.utils.PrepareDownloadUtils.serverDownloadPath;

public class FileDownloader implements Callable<Integer> {
    private final int blockSize;
    private final long fileSizeInMB;
    private  final FileInfoHolder fileInfoHolder;

    private Path filePath;
    private String fileName;
    private URL url;

    public long getFileSizeInMB() {
        return fileSizeInMB;
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
    public Integer call() {
        int blockNumber = 0;
        long transferredCount;

        try (ReadableByteChannel channel = Channels.newChannel(this.url.openStream())) {
            do {
                Path filePartPath = createFilePartPath(blockNumber);
                File partFile = filePartPath.toFile();
                blockNumber++;

                try (FileOutputStream fileOutputStream = new FileOutputStream(partFile); FileChannel fileOutputChannel = fileOutputStream.getChannel()) {
                    transferredCount = fileOutputChannel.transferFrom(channel, 0, blockSize);
                    fileInfoHolder.filesToProcess.add(filePartPath);
                } catch (SecurityException | IOException e) {
                    return handleDownloadError(e, "Cannot save to file: " + partFile.getAbsolutePath());
                }
            } while (transferredCount == blockSize);
        } catch (IOException e) {
            return handleDownloadError(e, "Cannot open given URL. Download aborted");
        }

        return 0;
    }

    private int handleDownloadError(Exception e, String message) {
        System.out.println(message);
        e.printStackTrace(System.out);

        removeFileParts(new ArrayList<>(fileInfoHolder.filesToProcess));

        return 1;
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
        return Paths.get(filePath + ".part" + partNumber);
    }
}
