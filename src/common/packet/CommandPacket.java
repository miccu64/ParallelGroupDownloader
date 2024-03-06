package common.packet;

import java.util.Arrays;

public class CommandPacket {
    private final String separator = "[<;>]";

    public CommandPacket(byte[] bytes) {
        String data = new String(bytes);
        if (!data.startsWith(separator)) {
            throw new RuntimeException("Packet does not have proper format.");
        }
        String[] splittedData = data.substring(separator.length()).split(separator);
        if (splittedData.length < 1) {
            throw new RuntimeException("Packet does not have proper format.");
        }

        int commandType = Integer.parseInt(splittedData[0]);
        if (commandType == CommandPacketTypes.InstanceJoined.ordinal()) {

        }
    }

    public CommandPacket(CommandPacketTypes type) {

    }
}
