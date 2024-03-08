package common.packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

public abstract class Command {
    public static final String separator = "&&&";

    protected final String message;
    protected final CommandType type;
    protected InetAddress source;

    public String getMessage() {
        return message;
    }

    public CommandType getType() {
        return type;
    }

    public InetAddress getSource() {
        return source;
    }

    protected Command(CommandType type, String message) {
        this.type = type;
        this.message = separator + type + separator + message + separator;
    }

    protected Command(CommandType type, String message, InetAddress source) {
        this(type, message);
        this.source = source;
    }

    public DatagramPacket createDatagram(InetAddress destination, int port) {
        byte[] data = message.getBytes();
        return new DatagramPacket(data, data.length, destination, port);
    }
}
