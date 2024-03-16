import common.DownloadStatusEnum;
import common.DownloaderException;
import common.UdpService;
import common.packet.Command;
import common.packet.CommandType;
import server.FileDownloader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainThread extends Thread implements PropertyChangeListener {
    private static final AtomicBoolean canTakeInput = new AtomicBoolean(true);
    private final CyclicBarrier cyclicBarrier;
    private final UdpService udpService;
    private FileDownloader downloader;
    private List<Path> processedFiles = new ArrayList<>();

    public MainThread(String multicastIp, int port, CyclicBarrier cyclicBarrier, String url) throws DownloaderException {
        this.cyclicBarrier = cyclicBarrier;

        udpService = new UdpService(multicastIp, port);
        udpService.start();

        if (url != null) {
            downloader = new FileDownloader(url, 1);
        }
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        DownloadStatusEnum downloaderStatus = ((DownloadStatusEnum) evt.getNewValue());
        Command command;
        switch (downloaderStatus) {
            case Error:
                // TODO: error handle on all instances
                command = new Command(CommandType.DownloadAbort);
                break;
            case DownloadedPart:
                command = new Command(CommandType.GotNextFilePart);
                // TODO: send info and init transfer
                break;
            case Success:
                command = new Command(CommandType.GotNextFilePart);
                // TODO: inform all about end
                break;
            default:
                return;
        }

        udpService.send(command);
        if (downloaderStatus == DownloadStatusEnum.Error) {
            udpService.interrupt();
            downloader.removeFileParts();

            System.exit(1);
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
                    if (downloader == null) {
                        System.out.println("No source URL was given when started program. Restart with provided URL");
                    } else {
                        downloader.addPropertyChangeListener(this);
                        downloader.run();
                        loop = false;
                        System.out.println("Download started...");
                    }
                    break;
                case "0":
                    udpService.interrupt();

                    System.exit(0);
                    break;
                default:
                    System.out.println("Wrong option\n");
            }
        }
    }
}
