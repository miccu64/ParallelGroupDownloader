package server;

import common.DownloadStatusEnum;
import common.DownloadException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static common.utils.FilePartUtils.removeFileParts;
import static common.utils.PrepareDownloadUtils.downloadPath;

public class FileDownloader implements Runnable {
    private static final ConcurrentLinkedQueue<Path> filePaths = new ConcurrentLinkedQueue<>();

    private final int blockSize;
    private final long fileSizeInMB;
    private final PropertyChangeSupport propertyChange;

    private Path filePath;
    private String fileName;
    private URL url;
    private DownloadStatusEnum downloadStatus;

    public void setDownloadStatus(DownloadStatusEnum value) {
        propertyChange.firePropertyChange("downloadStatus", this.downloadStatus, value);
        this.downloadStatus = value;
    }

    public long getFileSizeInMB() {
        return fileSizeInMB;
    }

    public FileDownloader(String url, int blockSizeInMB) throws DownloadException {
        propertyChange = new PropertyChangeSupport(this);

        try {
            URI uri = new URI(url);
            this.url = uri.toURL();
            fileName = getFileNameFromUrl(uri);
            filePath = Paths.get(String.valueOf(downloadPath), fileName);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new DownloadException(e, "Malformed URL");
        }

        blockSize = blockSizeInMB * 1024 * 1024;
        fileSizeInMB = findFileSizeInMB();

        downloadStatus = DownloadStatusEnum.Waiting;
    }

    public void run() {
        int blockNumber = 0;
        long transferredCount = 0;

        try (ReadableByteChannel channel = Channels.newChannel(this.url.openStream())) {
            do {
                Path filePartPath = createFilePartPath(blockNumber);
                File partFile = filePartPath.toFile();
                blockNumber++;

                try (FileOutputStream fileOutputStream = new FileOutputStream(partFile); FileChannel fileOutputChannel = fileOutputStream.getChannel()) {
                    transferredCount = fileOutputChannel.transferFrom(channel, 0, blockSize);
                    filePaths.add(filePartPath);
                    setDownloadStatus(DownloadStatusEnum.DownloadedPart);
                } catch (SecurityException | IOException e) {
                    handleDownloadError(e, "Cannot save to file: " + partFile.getAbsolutePath());
                }
            } while (transferredCount == blockSize);

            setDownloadStatus(DownloadStatusEnum.Success);
        } catch (IOException e) {
            handleDownloadError(e, "Cannot open given URL. Download aborted");
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChange.addPropertyChangeListener(pcl);
    }

    private void handleDownloadError(Exception e, String message) {
        setDownloadStatus(DownloadStatusEnum.Error);

        System.out.println(message);
        e.printStackTrace(System.out);

        removeFileParts(getFilePaths());
    }

    public List<Path> getFilePaths() {
        return Arrays.asList(filePaths.toArray(new Path[0]));
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
