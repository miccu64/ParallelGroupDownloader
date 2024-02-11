import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader {
    private final HttpURLConnection httpConnection;
    private final long fileSize;
    private final long blockSize;

    public FileDownloader(String _url, long _blockSize) throws IOException {
        blockSize = _blockSize;

        URL url = new URL(_url);
        httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("HEAD");
        fileSize = httpConnection.getContentLengthLong();
    }

    public void downloadBlock() {

    }
}
