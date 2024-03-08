package common.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public class CommandHelpers {
    private static final String separator = Command.separator;

    public static Command readPacket(DatagramPacket packet) {
        String message = new String(packet.getData(), 0, packet.getLength());
        String[] splitData = Arrays.stream(message.split(separator))
                .filter(str -> !str.isEmpty())
                .toArray(String[]::new);
        if (splitData.length < 1) {
            throw new RuntimeException("Packet does not have proper format.");
        }

        InetAddress address = packet.getAddress();
        CommandType type = CommandType.valueOf(splitData[0]);

        switch (type) {
            case FindOthers:
                return new CommandFindOthers(address);
            case ResponseToFindOthers:
                return new CommandRespondFindOthers(address);
            default:
                throw new IllegalArgumentException("Wrong command type.");
        }
    }
}
