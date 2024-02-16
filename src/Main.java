import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String url = "https://getsamplefiles.com/download/zip/sample-1.zip";
        int blockSizeInMB = 1;
        String fileName = "test.zip";

        FileDownloader downloader = new FileDownloader(url, blockSizeInMB, fileName);

        if (downloader.downloadWholeFile())
            downloader.joinDeleteFileParts();
    }
}