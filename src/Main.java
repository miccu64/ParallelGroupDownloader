import common.DownloaderException;
import common.PrepareDownloadUtils;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) throws IOException, DownloaderException {
        PrepareDownloadUtils.initProgram();

        int threadsCount = 3;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadsCount);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadsCount; i++){
            Thread thread = new MainThread("230.1.1.1", 10100, cyclicBarrier);
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("All threads have completed.");
    }
}