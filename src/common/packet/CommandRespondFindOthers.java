package common.packet;

import java.net.InetAddress;

public class CommandRespondFindOthers extends Command {
    public CommandRespondFindOthers(String message) {
        super(CommandType.ResponseToFindOthers, message);
    }

    public CommandRespondFindOthers(String message, InetAddress sourceAddress) {
        super(CommandType.ResponseToFindOthers, message, sourceAddress);
    }
}
