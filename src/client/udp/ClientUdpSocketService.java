package client.udp;

import common.DownloadException;
import common.udp.FileInfoHolder;
import common.udp.UdpSocketService;
import common.command.Command;
import common.command.CommandType;

public class ClientUdpSocketService extends UdpSocketService {
    public ClientUdpSocketService(String multicastIp, int port, FileInfoHolder fileInfoHolder) throws DownloadException {
        super(multicastIp, port, fileInfoHolder, new ClientUdpcastService(5000, fileInfoHolder));
    }

    @Override
    protected boolean actionsOnCommandReceive(Command command) {
        boolean result = super.actionsOnCommandReceive(command);

        CommandType type = command.getType();
        switch (type){
            case DownloadStart:
                break;
            case NextFilePart:
                break;
            case DownloadAbort:
                // TODO: clear deleted files
                break;
        }
        return result;
    }
}
