package client.udp;

import common.DownloadException;
import common.udp.FileInfoHolder;
import common.udp.SocketService;
import common.command.Command;
import common.command.CommandType;
import common.utils.PrepareDownloadUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientSocketService extends SocketService {
    public ClientSocketService(String multicastIp, int port, FileInfoHolder fileInfoHolder) throws DownloadException {
        super(multicastIp, port, fileInfoHolder, new ClientUdpcastService(5000, fileInfoHolder));
    }

    @Override
    protected boolean actionsOnCommandReceive(Command command) {
        boolean result = super.actionsOnCommandReceive(command);

        CommandType type = command.getType();
        switch (type){
            case BecameServer:
                fileInfoHolder.canBecomeServer.set(false);
                break;
            case DownloadStart:
                startUdpcastThread();
                break;
            case NextFilePart:
                String fileName = command.getData().get("fileName");
                Path path = Paths.get(String.valueOf(PrepareDownloadUtils.clientDownloadPath), fileName).toAbsolutePath();
                fileInfoHolder.filesToProcess.add(path);
                break;
            case DownloadAbort:
                // TODO: clear deleted files
                break;
        }
        return result;
    }
}
