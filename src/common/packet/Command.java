package common.packet;

import common.Socket;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public abstract class Command {
    public static final String separator = "&&&";

    protected final String message;
    protected final CommandType type;
    protected Socket sourceSocket;
    protected Socket destinationSocket;

    public String getMessage() {
        return message;
    }

    public CommandType getType() {
        return type;
    }

    public Socket getSourceSocket() {
        return sourceSocket;
    }

    public Socket getDestinationSocket() {
        return destinationSocket;
    }

    protected Command(CommandType type, String message, int sourcePort, Socket destination) {
        this.type = type;
        this.destinationSocket = destination;
        if (message == null)
            message = "";
        this.message = separator + type + separator + sourcePort + separator + message + separator;
    }

    protected Command(CommandType type, String message, InetAddress sourceAddress) {
        this.type = type;
        this.message = message;

        int port = Integer.parseInt(splitMessage()[1]);
        this.sourceSocket = new Socket(sourceAddress, port);
    }

    public DatagramPacket createDatagram() {
        byte[] data = message.getBytes();
        return new DatagramPacket(data, data.length, this.destinationSocket.getAddress(), this.destinationSocket.getPort());
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
