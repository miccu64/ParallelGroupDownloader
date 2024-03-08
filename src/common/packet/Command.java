package common.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public abstract class Command {
    public static final String separator = "&&&";

    protected final String message;
    protected final CommandType type;
    protected InetAddress sourceAddress;
    protected int sourcePort;

    public String getMessage() {
        return message;
    }

    public CommandType getType() {
        return type;
    }

    public InetAddress getSourceAddress() {
        return sourceAddress;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    protected Command(CommandType type, String message) {
        this.type = type;
        if (message == null)
            message = "";
        this.message = separator + type + separator + message + separator;
    }

    protected Command(CommandType type, String message, InetAddress source) {
        this.type = type;
        this.message = message;
        this.sourceAddress = source;

        String[] splitMessage = splitMessage();
        sourcePort = Integer.parseInt(splitMessage[1]);
    }

    public DatagramPacket createDatagram(InetAddress destination, int port) {
        byte[] data = message.getBytes();
        return new DatagramPacket(data, data.length, destination, port);
    }

    protected String[] splitMessage(){
        String[] splitData = Arrays.stream(message.split(separator))
            .filter(str -> !str.isEmpty())
            .toArray(String[]::new);

        if (splitData.length < 1)
            throw new RuntimeException("Packet does not have proper format.");

        return splitData;
    }
}
