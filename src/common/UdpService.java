package common;

import common.packet.Command;
import common.packet.CommandType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UdpService extends Thread {
    private final InetAddress group;
    private final MulticastSocket socket;
    private final byte[] buf = new byte[256];
    private final int port;

    public UdpService(String multicastIp, int port) throws IOException {
        this.port = port;
        this.group = InetAddress.getByName(multicastIp);

        socket = new MulticastSocket(this.port);
        socket.joinGroup(group);
    }

    public void run() {
        boolean loop = true;
        while (loop) {
            DatagramPacket datagram = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(datagram);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Command receivedCommand = new Command(datagram);

            System.out.println("Received: " + receivedCommand.getMessage());

            switch (receivedCommand.getType()) {
                case FindOthers:
                    Command command = new Command(CommandType.ResponseToFindOthers, "");
                    send(command);
                    break;
                case DownloadStart:
                    loop = false;
                    break;
            }
        }
    }

    public void send(Command command) {
        DatagramPacket packet = command.createDatagram(group, port);
        try {
            socket.send(packet);
            System.out.println("Sent: " + command.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
