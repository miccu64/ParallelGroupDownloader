import common.Socket;
import common.UdpService;
import common.packet.Command;
import common.packet.CommandFindOthers;
import common.packet.CommandHelpers;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class MainThread extends Thread {
    private static final AtomicBoolean canTakeInput = new AtomicBoolean(true);
    private final CyclicBarrier cyclicBarrier;
    private final List<Integer> otherInstancesPorts;
    private final int listenPort;
    private final UdpService udpService;

    public MainThread(int listenPort, int[] otherInstancesPorts, CyclicBarrier cyclicBarrier) throws SocketException {
        this.cyclicBarrier = cyclicBarrier;
        this.otherInstancesPorts = Arrays.stream(otherInstancesPorts)
            .boxed()
            .collect(Collectors.toList());
        this.otherInstancesPorts.remove(Integer.valueOf(listenPort));
        this.listenPort = listenPort;

        udpService = new UdpService(listenPort);
        udpService.start();
    }

    public void run() {
        try {
            cyclicBarrier.await();

            for (int destinationPort : otherInstancesPorts) {
                findOthers(destinationPort);
            }
            if (canTakeInput.compareAndSet(true, false)) {
                waitForInputInvokeDownload();
            }

            cyclicBarrier.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void findOthers(int destinationPort){
        try {
            Socket destinationSocket = new Socket(CommandHelpers.getBroadcastAddress(), destinationPort);
            Command packet = new CommandFindOthers(listenPort, destinationSocket);
            udpService.send(packet);
        } catch (UnknownHostException e){
            throw new RuntimeException(e);
        }
    }

    private void waitForInputInvokeDownload() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter download URL: ");
        String url = scanner.nextLine();
    }
}
