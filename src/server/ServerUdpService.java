package server;

import common.DownloadStatusEnum;
import common.DownloaderException;
import common.UdpService;
import common.packet.Command;
import common.packet.CommandType;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static common.FilePartUtils.fileChecksum;
import static common.FilePartUtils.removeFileParts;

public class ServerUdpService extends UdpService implements PropertyChangeListener {
    private final FileDownloader fileDownloader;

    private final List<Path> processedFiles = new ArrayList<>();
    private final List<Path> filesToProcess = new ArrayList<>();

    public ServerUdpService(String multicastIp, int port, String url) throws DownloaderException {
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
        Command command;
        switch (downloaderStatus) {
            case Error:
                command = new Command(CommandType.DownloadAbort);
                send(command);
                break;
            case DownloadedPart:
                List<Path> newPaths = fileDownloader.getFilePaths().stream()
                        .filter(path -> !processedFiles.contains(path))
                        .collect(Collectors.toList());
                filesToProcess.addAll(newPaths);

                for (Path path : newPaths) {
                    String checksum = fileChecksum(path);
                    HashMap<String, String> data = new HashMap<>();
                    data.put("Checksum", checksum);
                    data.put("FilePartName", path.getFileName().toString());

                    command = new Command(CommandType.NextFilePart, data);
                    send(command);
                }
                // TODO: init transfer
                break;
            case Success:
                HashMap<String, String> data = new HashMap<>();
                int partsCount = processedFiles.size() + filesToProcess.size();
                data.put("PartsCount", String.valueOf(partsCount));

                command = new Command(CommandType.Success, data);
                send(command);
                break;
            default:
                throw new RuntimeException("Not handled downloader status: " + downloaderStatus);
        }

        if (downloaderStatus == DownloadStatusEnum.Error) {
            LinkedList<Path> allFiles = new LinkedList<>();
            allFiles.addAll(processedFiles);
            allFiles.addAll(filesToProcess);
            removeFileParts(allFiles);

            System.exit(1);
        }
    }
}
