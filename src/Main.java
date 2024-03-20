import common.DownloaderException;
import common.PrepareDownloadUtils;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) throws DownloaderException {
        PrepareDownloadUtils.initProgram();

        int threadsCount = 3;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadsCount);
        for (int i = 0; i < threadsCount; i++){
            String url = "file:/home/lubuntu/Desktop/someFile.txt";
            Thread thread = new MainThread("230.1.1.1", 10100, url, cyclicBarrier);
            thread.start();
        }

        System.out.println("All threads have completed.");
    }
}