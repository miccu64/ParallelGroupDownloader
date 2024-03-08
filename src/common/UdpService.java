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

public class UdpService extends Thread {
    private DatagramSocket socket;
    private byte[] buf = new byte[256];
    private final int port;

    public UdpService(int port) throws SocketException {
        this.port = port;
        socket = new DatagramSocket(port);
    }

    public void run() {
        while (true) {
            DatagramPacket datagram = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(datagram);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Command command = CommandHelpers.readPacket(datagram);
            System.out.println("Received from: " + command.getSource() + ", data: " + command.getMessage());

            if (command.getType() == CommandType.FindOthers){
                DatagramPacket packet = new CommandRespondFindOthers().createDatagram(command.getSource(), port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //socket.close();
    }

    public void sendBroadcast(Command packet) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
        String message = packet.getMessage();
        byte[] data = message.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(data, data.length, broadcastAddress, port);
        socket.send(sendPacket);
        socket.close();

        System.out.println("Sent on broadcast: " + message);
    }
}
