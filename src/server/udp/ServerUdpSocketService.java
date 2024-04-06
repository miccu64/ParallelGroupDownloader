package server.udp;

import common.DownloadException;
import common.command.Command;
import common.command.CommandType;
import common.udp.UdpSocketService;
import server.FileDownloader;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ServerUdpSocketService extends UdpSocketService {
    private final Future<Integer> fileDownloaderFuture;
    private final Thread udpcastThread;

    public ServerUdpSocketService(String multicastIp, int port, String url) throws DownloadException {
        super(multicastIp, port);

        ServerUdpcastService serverUdpcastService = new ServerUdpcastService(5000, this.fileInfoHolder);
        udpcastThread = new Thread(serverUdpcastService);
        udpcastThread.start();

        FileDownloader fileDownloader = new FileDownloader(url, 1, this.fileInfoHolder);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        fileDownloaderFuture = executorService.submit(fileDownloader);
        // TODO: listen to fileDownloaderFuture
    }

    @Override
    protected boolean actionsOnCommandReceive(Command command) {
        boolean result = super.actionsOnCommandReceive(command);

        CommandType type = command.getType();

        return result;
    }

    @Override
    public void close() {
        super.close();
        udpcastThread.interrupt();
    }

    private void handleDownloaderError() {
        Command command = new Command(CommandType.DownloadAbort);
        send(command);

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
