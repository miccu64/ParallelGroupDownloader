import client.udp.ClientUdpSocketService;
import common.DownloadException;
import common.udp.UdpSocketService;
import common.command.Command;
import common.command.CommandType;
import server.udp.ServerUdpSocketService;

import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static common.utils.PrepareDownloadUtils.checkIsValidUrl;

public class MainThread extends Thread {
    private static final AtomicBoolean canTakeInput = new AtomicBoolean(true);
    private final CyclicBarrier cyclicBarrier;
    private final String multicastIp;
    private final int port;

    private UdpSocketService udpSocketService;
    private Thread udpServiceThread;
    private String url;

    public MainThread(String multicastIp, int port, String url, CyclicBarrier cyclicBarrier) throws DownloadException {
        this.multicastIp = multicastIp;
        this.port = port;
        this.url = url;
        this.cyclicBarrier = cyclicBarrier;

        checkIsValidUrl(url);

        udpSocketService = new ClientUdpSocketService(multicastIp, port);
        udpServiceThread = new Thread(udpSocketService);
        udpServiceThread.start();
    }

    public void run() {
        try {
            cyclicBarrier.await();

            Command packet = new Command(CommandType.FindOthers);
            udpSocketService.send(packet);

            if (canTakeInput.compareAndSet(true, false)) {
                waitForUserInput();
            }

            cyclicBarrier.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForUserInput() {
        Scanner scanner = new Scanner(System.in);

        boolean loop = true;
        while (loop) {
            System.out.println("1) Start downloading");
            System.out.println("0) Shut down instance");
            System.out.print("Choose option: ");

            String input;
            try {
                input = scanner.nextLine();
            } catch (Exception ignored) {
                return;
            }

            switch (input) {
                case "1":
                    if (url == null) {
                        System.out.println("No source URL was given when started program. Restart with provided URL");
                    } else {
                        Map<String, String> data = new HashMap<>();
                        data.put("Url", url);
                        Command command = new Command(CommandType.DownloadStart, data);
                        udpSocketService.send(command);
                        udpServiceThread.interrupt();
                        udpSocketService.close();

                        try {
                            udpSocketService = new ServerUdpSocketService(multicastIp, port, url);
                            udpServiceThread = new Thread(udpSocketService);
                            udpServiceThread.start();
                            System.out.println("Download started...");
                        } catch (DownloadException ignored) {
                            System.exit(1);
                        }

                        loop = false;
                    }
                    break;
                case "0":
                    udpServiceThread.interrupt();
                    udpSocketService.close();

                    System.exit(0);
                    break;
                default:
                    System.out.println("Wrong option\n");
            }
        }
    }
}
