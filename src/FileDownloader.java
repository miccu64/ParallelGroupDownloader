import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileDownloader {
    private final int blockSize;
    private final Path filePath;
    private final URL url;
    private int blockNumber = 0;

    public FileDownloader(String _url, int _blockSizeInMB, String _fileName) throws IOException {
        url = new URL(_url);

        blockSize = _blockSizeInMB * 1024 * 1024;
        String downloadFolderName = "downloads";
        filePath = Paths.get(downloadFolderName, _fileName);

        Files.createDirectories(Paths.get(downloadFolderName));
    }

    public boolean downloadWholeFile() {
        blockNumber = 0;
        long transferredCount;

        try {
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            do {
                File partFile = createFilePartPath(blockNumber).toFile();
                blockNumber++;

                try (FileOutputStream fileOutputStream = new FileOutputStream(partFile)) {
                    FileChannel fileOutputChannel = fileOutputStream.getChannel();
                    transferredCount = fileOutputChannel.transferFrom(readableByteChannel, 0, blockSize);
                } catch (FileNotFoundException | SecurityException e) {
                    return handleException(e, "Cannot save to file: " + partFile.getAbsolutePath());
                } catch (Exception e) {
                    return handleException(e, "Problem with downloading file");
                }
            } while (transferredCount == blockSize);
        } catch (IOException e) {
            return handleException(e, "Cannot open URL. Download aborted.");
        }

        return true;
    }

    public boolean joinFileParts() {
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

        return true;
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
