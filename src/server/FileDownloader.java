package server;

import common.DownloaderException;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static common.PrepareDownloadUtils.downloadPath;

public class FileDownloader {
    private final int blockSize;
    private final Path filePath;
    private final String fileName;
    private final URL url;
    private final long fileSizeInMB;

    private int blockNumber = 0;

    public long getFileSizeInMB() {
        return fileSizeInMB;
    }

    public String getFileName() {
        return fileName;
    }

    public FileDownloader(String url, int blockSizeInMB) throws DownloaderException {
        URI uri;
        try {
            uri = new URI(url);
            this.url = uri.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new DownloaderException(e, "Malformed URL");
        }

        blockSize = blockSizeInMB * 1024 * 1024;
        fileName = getFileNameFromUrl(uri);

        filePath = Paths.get(String.valueOf(downloadPath), fileName);

        this.fileSizeInMB = findFileSizeInMB();
    }

    public void downloadWholeFile() throws DownloaderException {
        blockNumber = 0;
        long transferredCount;

        try (ReadableByteChannel channel = Channels.newChannel(this.url.openStream())) {
            do {
                File partFile = createFilePartPath(blockNumber).toFile();
                blockNumber++;

                try (FileOutputStream fileOutputStream = new FileOutputStream(partFile); FileChannel fileOutputChannel = fileOutputStream.getChannel()) {
                    transferredCount = fileOutputChannel.transferFrom(channel, 0, blockSize);
                } catch (SecurityException | IOException e) {
                    throw new DownloaderException(e, "Cannot save to file: " + partFile.getAbsolutePath());
                }
            } while (transferredCount == blockSize);
        } catch (IOException e) {
            throw new DownloaderException(e, "Cannot open given URL. Download aborted");
        }
    }

    public boolean joinDeleteFileParts() {
        try {
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
        } catch (IOException e) {
            return handleException(e, "Cannot create result file: " + filePath);
        }

        for (int i = 0; i < blockNumber; i++) {
            Path filePartPath = createFilePartPath(i);

            try (OutputStream out = Files.newOutputStream(filePath, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                Files.copy(filePartPath, out);
            } catch (IOException e) {
                return handleException(e, "Error while joining parts of file");
            }
        }

        for (int i = 0; i < blockNumber; i++) {
            Path filePartPath = createFilePartPath(i);
            try {
                Files.deleteIfExists(filePartPath);
            } catch (IOException ignored) {
            }
        }

        return true;
    }

    private String getFileNameFromUrl(URI uri){
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
        }

        return fileSize;
    }

    private Path createFilePartPath(int partNumber) {
        return Paths.get(filePath + ".part" + partNumber);
    }

    private boolean handleException(Exception e, String message) {
        System.out.println(message);
        e.printStackTrace(System.out);
        return false;
    }
}
