package common.packet;

import java.net.InetAddress;

public class CommandRespondFindOthers extends Command {
    public CommandRespondFindOthers() {
        super(CommandType.ResponseToFindOthers, "");
    }

    public CommandRespondFindOthers(InetAddress sourceAddress) {
        super(CommandType.ResponseToFindOthers, "", sourceAddress);
    }
}
