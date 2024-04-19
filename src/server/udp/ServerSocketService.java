package server.udp;

import common.DownloadException;
import common.command.Command;
import common.command.CommandType;
import common.udp.FileInfoHolder;
import common.udp.SocketService;
import server.FileDownloader;

import java.util.HashMap;

public class ServerSocketService extends SocketService {
    private final Thread fileDownloaderThread;

    public ServerSocketService(String multicastIp, int port, String url, FileInfoHolder fileInfoHolder) throws DownloadException {
        super(multicastIp, port, fileInfoHolder, new ServerUdpcastService(5000, fileInfoHolder));

        SendCommandCallback callback = filePartPath -> {
            HashMap<String, String> data = new HashMap<>();
            data.put("fileName", String.valueOf(filePartPath.getFileName()));

            Command command = new Command(CommandType.NextFilePart, data);
            send(command);
        };
        FileDownloader fileDownloader = new FileDownloader(url, 1, fileInfoHolder, callback);
        fileDownloaderThread = new Thread(() -> {
            int result = fileDownloader.call();
            if (result == 0) {
                handleDownloaderSuccess();
            } else {
                handleDownloaderError();
            }
        });
        fileDownloaderThread.start();

        startUdpcastThread();
    }

    @Override
    protected boolean actionsOnCommandReceive(Command command) {
        boolean result = super.actionsOnCommandReceive(command);

        CommandType type = command.getType();
        if (type == CommandType.BecameServer){
            // TODO:  error
        }

        return result;
    }

    @Override
    public void close() {
        super.close();
        fileDownloaderThread.interrupt();
    }

    private void handleDownloaderError() {
        Command command = new Command(CommandType.DownloadAbort);
        send(command);

        fileInfoHolder.setErrorStatus();

        close();
        System.exit(1);
    }

    private void handleDownloaderSuccess() {
        HashMap<String, String> data = new HashMap<>();
        // TODO: give proper value
        int partsCount = 1;
        data.put("PartsCount", String.valueOf(partsCount));

        Command command = new Command(CommandType.Success, data);
        send(command);
    }

    private void handleDownloaderNewFileParts() {
//        for (Path path : newPaths) {
//            String checksum = fileChecksum(path);
//            HashMap<String, String> data = new HashMap<>();
//            data.put("Checksum", checksum);
//            data.put("FilePartName", path.getFileName().toString());
//
//            Command command = new Command(CommandType.NextFilePart, data);
//            send(command);
//        }
    }
}
