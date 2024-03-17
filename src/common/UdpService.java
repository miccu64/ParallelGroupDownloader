package common;

import common.packet.Command;
import common.packet.CommandType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UdpService extends Thread implements IUdpService {
    private final InetAddress group;
    private final MulticastSocket socket;
    private final byte[] buf = new byte[256];
    private final int port;

    public UdpService(String multicastIp, int port) throws DownloaderException {
        this.port = port;
        try {
            this.group = InetAddress.getByName(multicastIp);
            socket = new MulticastSocket(this.port);
            socket.joinGroup(group);
        } catch (IOException e){
            throw new DownloaderException(e, "Could not join to multicast: " + multicastIp + ":" + port);
        }
    }

    public void run() {
        boolean loop = true;
        while (loop) {
            DatagramPacket datagram = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(datagram);

                Command receivedCommand = new Command(datagram);
                System.out.println("Received from " + datagram.getSocketAddress() + ": " + receivedCommand);

                switch (receivedCommand.getType()) {
                    case FindOthers:
                        Command command = new Command(CommandType.ResponseToFindOthers);
                        send(command);
                        break;
                    case DownloadStart:
                        loop = false;
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (DownloaderException ignored) {
            }
        }
    }

    public void send(Command command) {
        DatagramPacket packet = command.createDatagram(group, port);
        try {
            socket.send(packet);
            System.out.println("Sent: " + command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
