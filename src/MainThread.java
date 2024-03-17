import common.DownloadStatusEnum;
import common.DownloaderException;
import common.UdpService;
import common.packet.Command;
import common.packet.CommandType;
import server.FileDownloader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static common.FilePartUtils.*;

public class MainThread extends Thread implements PropertyChangeListener {
    private static final AtomicBoolean canTakeInput = new AtomicBoolean(true);
    private final CyclicBarrier cyclicBarrier;
    private final UdpService udpService;
    private FileDownloader downloader;
    private final List<Path> processedFiles = new ArrayList<>();
    private final List<Path> filesToProcess = new ArrayList<>();

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
                command = new Command(CommandType.DownloadAbort);
                udpService.send(command);
                break;
            case DownloadedPart:
                List<Path> newPaths = downloader.getFilePaths().stream()
                        .filter(path -> !processedFiles.contains(path))
                        .collect(Collectors.toList());
                filesToProcess.addAll(newPaths);

                for (Path path : newPaths) {
                    String checksum = fileChecksum(path);
                    HashMap<String, String> data = new HashMap<>();
                    data.put("Checksum", checksum);
                    data.put("FilePartName", path.getFileName().toString());

                    command = new Command(CommandType.NextFilePart, data);
                    udpService.send(command);
                }
                // TODO: init transfer
                break;
            case Success:
                HashMap<String, String> data = new HashMap<>();
                int partsCount = processedFiles.size() + filesToProcess.size();
                data.put("PartsCount", String.valueOf(partsCount));

                command = new Command(CommandType.Success, data);
                udpService.send(command);
                break;
            default:
                throw new RuntimeException("Not handled downloader status: " + downloaderStatus);
        }

        if (downloaderStatus == DownloadStatusEnum.Error) {
            udpService.interrupt();

            LinkedList<Path> allFiles = new LinkedList<>();
            allFiles.addAll(processedFiles);
            allFiles.addAll(filesToProcess);
            removeFileParts(allFiles);

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
