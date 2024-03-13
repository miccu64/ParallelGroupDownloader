package common.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public class Command {
    public static final String separator = "&&&";

    private final String message;
    private final CommandType type;

    public String getMessage() {
        return message;
    }

    public CommandType getType() {
        return type;
    }

    public Command(CommandType type, String message) {
        this.type = type;
        if (message == null)
            message = "";
        this.message = separator + type + separator + message + separator;
    }

    public Command(DatagramPacket packet) {
        message = new String(packet.getData(), 0, packet.getLength());
        String[] splitData = Arrays.stream(message.split(separator))
                .filter(str -> !str.isEmpty())
                .toArray(String[]::new);
        if (splitData.length < 1) {
            throw new RuntimeException("Packet does not have proper format.");
        }

        InetAddress address = packet.getAddress();
        type = CommandType.valueOf(splitData[0]);
    }

    public DatagramPacket createDatagram(InetAddress address, int port) {
        byte[] data = message.getBytes();

        return new DatagramPacket(data, data.length, address, port);
    }

    private String[] splitMessage(){
        String[] splitData = Arrays.stream(message.split(separator))
            .filter(str -> !str.isEmpty())
            .toArray(String[]::new);

        if (splitData.length < 1)
            throw new RuntimeException("Packet does not have proper format.");

        return splitData;
    }
}
