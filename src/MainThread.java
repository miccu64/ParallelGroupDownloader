import common.DownloadStatusEnum;
import common.DownloaderException;
import common.UdpService;
import common.packet.Command;
import common.packet.CommandType;
import server.FileDownloader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Scanner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainThread extends Thread implements PropertyChangeListener {
    private static final AtomicBoolean canTakeInput = new AtomicBoolean(true);
    private final CyclicBarrier cyclicBarrier;
    private final UdpService udpService;
    private FileDownloader downloader;

    public MainThread(String multicastIp, int port, CyclicBarrier cyclicBarrier, String url) throws DownloaderException {
        this.cyclicBarrier = cyclicBarrier;

        udpService = new UdpService(multicastIp, port);
        udpService.start();

        if (url != null){
            downloader = new FileDownloader(url, 1);
        }
    }

    public void run() {
        try {
            cyclicBarrier.await();

            Command packet = new Command(CommandType.FindOthers, null);
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
        DownloadStatusEnum status = ((DownloadStatusEnum) evt.getNewValue());
    }

    private void waitForUserInput() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("1) Start downloading");
            System.out.println("0) Shut down instance");
            System.out.print("Choose option: ");

            String input = scanner.nextLine();
            switch (input){
                case "1":
                    downloader.addPropertyChangeListener(this);
                    downloader.run();
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
