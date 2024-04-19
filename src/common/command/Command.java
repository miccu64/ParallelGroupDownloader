package common.command;

import common.DownloadException;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Map;

public class Command {
    private final CommandData data;

    public CommandType getType() {
        return data.getCommandType();
    }

    public Map<String, String> getData() {
        return data.getData();
    }

    public Command(CommandType type) {
        this(type, null);
    }

    public Command(CommandType type, Map<String, String> data) {
        this.data = new CommandData(type, data);
    }

    public Command(DatagramPacket packet) throws DownloadException {
        String message = new String(packet.getData(), 0, packet.getLength());
        data = new CommandData(message);
    }

    public DatagramPacket createDatagram(InetAddress address, int port) {
        byte[] data = this.data.toString().getBytes();

        return new DatagramPacket(data, data.length, address, port);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
