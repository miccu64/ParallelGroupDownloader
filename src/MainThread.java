import client.ClientUdpService;
import com.sun.security.ntlm.Client;
import common.DownloadStatusEnum;
import common.DownloaderException;
import common.UdpService;
import common.packet.Command;
import common.packet.CommandType;
import server.FileDownloader;
import server.ServerUdpService;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static common.FilePartUtils.*;

public class MainThread extends Thread {
    private static final AtomicBoolean canTakeInput = new AtomicBoolean(true);
    private final CyclicBarrier cyclicBarrier;
    private final String multicastIp;
    private final int port;

    private UdpService udpService;
    private Thread udpServiceThread;
    private String url;

    public MainThread(String multicastIp, int port, String url, CyclicBarrier cyclicBarrier) throws DownloaderException {
        this.multicastIp = multicastIp;
        this.port = port;
        this.url = url;
        this.cyclicBarrier = cyclicBarrier;

        udpService = new ClientUdpService(multicastIp, port);
        udpServiceThread = new Thread(udpService);
        udpServiceThread.start();
    }

    public void run() {
        try {
            cyclicBarrier.await();

            Command packet = new Command(CommandType.FindOthers);
            udpService.send(packet);

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

            String input = scanner.nextLine();
            switch (input) {
                case "1":
                    if (url == null) {
                        System.out.println("No source URL was given when started program. Restart with provided URL");
                    } else {
                        udpServiceThread.interrupt();
                        udpService.close();

                        try {
                            udpService = new ServerUdpService(multicastIp, port, url);
                            udpServiceThread = new Thread(udpService);
                            udpServiceThread.start();
                            System.out.println("Download started...");
                        } catch (DownloaderException ignored) {
                            System.exit(1);
                        }

                        loop = false;
                    }
                    break;
                case "0":
                    udpServiceThread.interrupt();

                    System.exit(0);
                    break;
                default:
                    System.out.println("Wrong option\n");
            }
        }
    }
}
