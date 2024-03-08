package common.packet;

import java.net.InetAddress;

public class CommandFindOthers extends Command {
    public CommandFindOthers(String message) {
        super(CommandType.FindOthers, message);
    }

    public CommandFindOthers(String message, InetAddress sourceAddress) {
        super(CommandType.FindOthers, message, sourceAddress);
    }
}