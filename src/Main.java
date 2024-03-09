import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) throws SocketException {
        int[] otherInstancesPorts = {10100, 10101, 10102} ;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(otherInstancesPorts.length);
        List<Thread> threads = new ArrayList<>();
        for (Integer port : otherInstancesPorts){
            Thread thread = new MainThread(port, otherInstancesPorts, cyclicBarrier);
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