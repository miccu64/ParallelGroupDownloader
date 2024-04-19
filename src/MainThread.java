import client.udp.ClientSocketService;
import common.DownloadException;
import common.udp.FileInfoHolder;
import common.udp.SocketService;
import common.command.Command;
import common.command.CommandType;
import server.udp.ServerSocketService;

import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

import static common.utils.PrepareDownloadUtils.checkIsValidUrl;

public class MainThread extends Thread {
    private static final AtomicBoolean canTakeInput = new AtomicBoolean(true);
    private final CyclicBarrier cyclicBarrier;


    private final FileInfoHolder fileInfoHolder = new FileInfoHolder();
    private final String multicastIp;
    private final int port;

    private SocketService socketService;
    private Thread socketServiceThread;
    private String url;

    public MainThread(String multicastIp, int port, String url, CyclicBarrier cyclicBarrier) throws DownloadException {
        this.multicastIp = multicastIp;
        this.port = port;
        this.url = url;
        this.cyclicBarrier = cyclicBarrier;

        checkIsValidUrl(url);

        socketService = new ClientSocketService(multicastIp, port, fileInfoHolder);
        socketServiceThread = new Thread(socketService);
        socketServiceThread.start();
    }

    public void run() {
        try {
            cyclicBarrier.await();

            Command packet = new Command(CommandType.FindOthers);
            socketService.send(packet);

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
                //input = scanner.nextLine();
                input = "1";
            } catch (Exception ignored) {
                return;
            }

            switch (input) {
                case "1":
                    if (url == null) {
                        System.out.println("No source URL was given when started program. Restart with provided URL");
                    } else {
                        if (!fileInfoHolder.canBecomeServer.get()){
                            break;
                        }

                        Command command = new Command(CommandType.BecameServer);
                        socketService.send(command);

                        socketServiceThread.interrupt();
                        socketService.close();
                        try {
                            socketService = new ServerSocketService(multicastIp, port, url, fileInfoHolder);
                            socketServiceThread = new Thread(socketService);
                            socketServiceThread.start();

                            HashMap<String, String> data = new HashMap<>();
                            data.put("url", url);
                            command = new Command(CommandType.DownloadStart, data);
                            socketService.send(command);

                            System.out.println("Download started...");
                        } catch (DownloadException ignored) {
                            System.exit(1);
                        }

                        loop = false;
                    }
                    break;
                case "0":
                    socketServiceThread.interrupt();
                    socketService.close();

                    System.exit(0);
                    break;
                default:
                    System.out.println("Wrong option\n");
            }
        }
    }
}
