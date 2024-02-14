import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class FileDownloader {
    private final int blockSize;
    private final String fileName;
    private final BufferedInputStream input;

    public FileDownloader(String _url, int _blockSize, String _fileName) throws IOException {
        blockSize = _blockSize;
        fileName = _fileName;

        input = new BufferedInputStream(new URL(_url).openStream());
    }

    public boolean downloadBlock(int blockNumber) throws IOException {
        String partFileName = fileName + "." + blockNumber;
        int bytesRead;

        try (FileOutputStream fileOutputStream = new FileOutputStream(partFileName)) {
            int startByte = blockNumber * blockSize;
            byte[] dataBuffer = new byte[blockSize];

            do {
                bytesRead = input.read(dataBuffer, startByte, startByte + blockSize);
            } while (bytesRead != -1 && bytesRead != 0 && bytesRead != blockSize);

            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }

        return bytesRead != blockSize;
    }

    public void joinParts() {

    }
}
