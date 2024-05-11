package server;

import common.models.StatusEnum;
import common.exceptions.DownloadException;
import common.utils.FilePartUtils;

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

public class FileDownloader implements Callable<StatusEnum> {
    private final ConcurrentLinkedQueue<Path> processedFiles = new ConcurrentLinkedQueue<>();
    private final long blockSizeInBytes;
    private final int fileSizeInMB;
    private final Path filePath;
    private final String fileName;
    private final URL url;

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

    public FileDownloader(String urlString, int blockSizeInMB, String downloadPath) throws DownloadException {
        if (urlString == null || urlString.isEmpty()) {
            throw new DownloadException("Empty url.");
        }

        try {
            URI uri = parseUri(urlString);
            this.url = uri.toURL();

            tryCheckIsFile(uri);

            fileName = getFileName(uri);
            filePath = Paths.get(downloadPath, fileName);
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
                Path filePartPath = createFilePartPath(blockNumber);
                File partFile = filePartPath.toFile();
                blockNumber++;

                try (FileOutputStream fileOutputStream = new FileOutputStream(partFile);
                     FileChannel fileOutputChannel = fileOutputStream.getChannel()) {
                    transferredCount = fileOutputChannel.transferFrom(channel, 0, blockSizeInBytes);
                } catch (SecurityException | IOException e) {
                    FilePartUtils.removeFile(filePartPath);
                    return handleDownloadError(e, "Cannot save to file: " + partFile);
                }

                if (transferredCount > 0) {
                    processedFiles.add(filePartPath);
                } else {
                    FilePartUtils.removeFile(filePartPath);
                }
            } while (transferredCount == blockSizeInBytes);
        } catch (IOException e) {
            return handleDownloadError(e, "Cannot open given URL. Download aborted");
        }

        return StatusEnum.Success;
    }

    private URI parseUri(String urlString) {
        URI uri;
        try {
            uri = new URI(urlString);
        } catch (Exception e) {
            uri = Paths.get(urlString).toUri();
        }
        return uri;
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

    private StatusEnum handleDownloadError(Exception e, String message) {
        System.out.println(message);
        e.printStackTrace(System.out);

        FilePartUtils.removeFiles(new ArrayList<>(processedFiles));
        return StatusEnum.Error;
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

    private Path createFilePartPath(int partNumber) {
        return Paths.get(filePath + ".part" + partNumber).toAbsolutePath();
    }
}
