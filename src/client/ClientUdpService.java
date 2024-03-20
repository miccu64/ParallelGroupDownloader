package client;

import common.DownloaderException;
import common.UdpService;
import common.packet.Command;
import common.packet.CommandType;

public class ClientUdpService extends UdpService {
    public ClientUdpService(String multicastIp, int port) throws DownloaderException {
        super(multicastIp, port);
    }

    @Override
    protected boolean actionsOnCommandReceive(Command command) {
        boolean result = super.actionsOnCommandReceive(command);

        CommandType type = command.getType();

        return result;
    }
}
