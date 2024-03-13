import common.PrepareDownloadUtils;
import common.UdpService;
import common.packet.Command;
import common.packet.CommandType;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainThread extends Thread {
    private static final AtomicBoolean canTakeInput = new AtomicBoolean(true);
    private final CyclicBarrier cyclicBarrier;
    private final int port;
    private final UdpService udpService;

    public MainThread(String multicastIp, int port, CyclicBarrier cyclicBarrier) throws IOException {
        this.cyclicBarrier = cyclicBarrier;
        this.port = port;

        udpService = new UdpService(multicastIp, port);
        udpService.start();
    }

    public void run() {
        try {
            cyclicBarrier.await();

            Command packet = new Command(CommandType.FindOthers, "");
            udpService.send(packet);

            if (canTakeInput.compareAndSet(true, false)) {
                String url = waitForInputInvokeDownload();
                //CommandDownloadStart command = new CommandDownloadStart(url, listenPort, udpService.getGatheredSockets())
            }

            cyclicBarrier.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String waitForInputInvokeDownload() {
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter download URL: ");
            String url = scanner.nextLine();
            if (PrepareDownloadUtils.isValidUrl(url)) {
                return url;
            } else {
                System.out.println("Invalid URL");
            }
        }
    }
}
