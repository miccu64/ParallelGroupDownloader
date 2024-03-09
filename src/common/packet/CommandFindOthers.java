package common.packet;

import common.Socket;

import java.net.InetAddress;

public class CommandFindOthers extends Command {
    public CommandFindOthers(int sourcePort, Socket destination) {
        super(CommandType.FindOthers, "", sourcePort, destination);
    }

    public CommandFindOthers(String message, InetAddress sourceAddress) {
        super(CommandType.FindOthers, message, sourceAddress);
    }
}