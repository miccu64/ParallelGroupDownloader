package common.packet;

import common.Socket;

import java.net.InetAddress;

public class CommandRespondFindOthers extends Command {
    public CommandRespondFindOthers(int sourcePort, Socket destination) {
        super(CommandType.ResponseToFindOthers, "", sourcePort, destination);
    }

    public CommandRespondFindOthers(String message, InetAddress sourceAddress) {
        super(CommandType.ResponseToFindOthers, message, sourceAddress);
    }
}
