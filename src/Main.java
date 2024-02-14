import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String url = "https://raw.githubusercontent.com/dscape/spell/master/test/resources/big.txt";
        int blockSize = 2222;
        String fileName = "test";

        FileDownloader downloader = new FileDownloader(url, blockSize, fileName);
        downloader.downloadBlock(0);
        downloader.downloadBlock(1);
    }
}