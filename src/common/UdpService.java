package common;

import common.packet.Command;
import common.packet.CommandHelpers;
import common.packet.CommandRespondFindOthers;
import common.packet.CommandType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

public class UdpService extends Thread {
    private final DatagramSocket socket;
    private final byte[] buf = new byte[256];
    private final int listenPort;
    private final List<Integer> differentInstancesPorts;

    public UdpService(int listenPort, List<Integer> differentInstancesPorts) throws SocketException {
        this.listenPort = listenPort;
        this.differentInstancesPorts = differentInstancesPorts;
        socket = new DatagramSocket(this.listenPort);
    }

    public void run() {
        while (true) {
            DatagramPacket datagram = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(datagram);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Command receivedCommand = CommandHelpers.readPacket(datagram);
            System.out.println("Received from: " + receivedCommand.getSourceAddress() + ":" + datagram.getPort() + ", data: " + receivedCommand.getMessage());

            if (receivedCommand.getType() == CommandType.FindOthers) {
                Command command = new CommandRespondFindOthers(String.valueOf(this.listenPort));
                sendUnicast(command, receivedCommand.getSourceAddress(), receivedCommand.getSourcePort());
            }
        }
        //socket.close();
    }

    public void sendUnicast(Command command, InetAddress destination, int port) {
        DatagramPacket packet = command.createDatagram(destination, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendBroadcast(Command packet) throws IOException {
        InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
        String message = packet.getMessage();
        byte[] data = message.getBytes();

        for (int port : differentInstancesPorts) {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            DatagramPacket sendPacket = new DatagramPacket(data, data.length, broadcastAddress, port);
            socket.send(sendPacket);

            socket.close();
        }
        System.out.println("Sent on broadcast: " + message);
    }
}
