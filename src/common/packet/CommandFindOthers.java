package common.packet;

import java.net.InetAddress;

public class CommandFindOthers extends Command {
    public CommandFindOthers() {
        super(CommandType.FindOthers, "");
    }

    public CommandFindOthers(InetAddress sourceAddress) {
        super(CommandType.FindOthers, "", sourceAddress);
    }
}