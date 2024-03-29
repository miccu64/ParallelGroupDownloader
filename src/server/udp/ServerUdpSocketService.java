package server.udp;

import common.DownloadStatusEnum;
import common.DownloadException;
import common.udp.UdpSocketService;
import common.command.Command;
import common.command.CommandType;
import server.FileDownloader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static common.utils.FilePartUtils.fileChecksum;

public class ServerUdpSocketService extends UdpSocketService implements PropertyChangeListener {
    private final FileDownloader fileDownloader;

    private final ConcurrentLinkedQueue<Path> processedFiles = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Path> filesToProcess = new ConcurrentLinkedQueue<>();

    public ServerUdpSocketService(String multicastIp, int port, String url) throws DownloadException {
        super(multicastIp, port);

        fileDownloader = new FileDownloader(url, 1);
    }

    @Override
    protected boolean actionsOnCommandReceive(Command command) {
        boolean result = super.actionsOnCommandReceive(command);

        CommandType type = command.getType();

        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        DownloadStatusEnum downloaderStatus = ((DownloadStatusEnum) evt.getNewValue());
        switch (downloaderStatus) {
            case Error:
                handleDownloaderError();
                return;
            case DownloadedPart:
                handleDownloaderNewFileParts();
                // TODO: init transfer
                break;
            case Success:
                handleDownloaderSuccess();
                break;
            default:
                throw new RuntimeException("Not handled downloader status: " + downloaderStatus);
        }
    }

    private void handleDownloaderError() {
        Command command = new Command(CommandType.DownloadAbort);
        send(command);

        close();
        System.exit(1);
    }

    private void handleDownloaderSuccess() {
        HashMap<String, String> data = new HashMap<>();
        // TODO: possible race
        int partsCount = processedFiles.size() + filesToProcess.size();
        data.put("PartsCount", String.valueOf(partsCount));

        Command command = new Command(CommandType.Success, data);
        send(command);
    }

    private void handleDownloaderNewFileParts() {
        List<Path> newPaths = fileDownloader.getFilePaths().stream()
                .filter(path -> !processedFiles.contains(path))
                .collect(Collectors.toList());
        filesToProcess.addAll(newPaths);

        for (Path path : newPaths) {
            String checksum = fileChecksum(path);
            HashMap<String, String> data = new HashMap<>();
            data.put("Checksum", checksum);
            data.put("FilePartName", path.getFileName().toString());

            Command command = new Command(CommandType.NextFilePart, data);
            send(command);
        }
    }
}
