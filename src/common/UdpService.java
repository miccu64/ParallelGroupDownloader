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
import java.util.concurrent.ConcurrentHashMap;

public class UdpService extends Thread {
    private final DatagramSocket socket;
    private final byte[] buf = new byte[256];
    private final int listenPort;
    private final List<Integer> differentInstancesPorts;
    private final ConcurrentHashMap<String, Socket> gatheredSockets;

    public UdpService(int listenPort, List<Integer> differentInstancesPorts) throws SocketException {
        this.listenPort = listenPort;
        this.differentInstancesPorts = differentInstancesPorts;
        socket = new DatagramSocket(this.listenPort);
        gatheredSockets = new ConcurrentHashMap<>();
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
            Socket sourceSocket = receivedCommand.getSourceSocket();
            gatheredSockets.putIfAbsent(sourceSocket.toString(), sourceSocket);

            System.out.println("Received from: " + sourceSocket + ", data: " + receivedCommand.getMessage());

            if (receivedCommand.getType() == CommandType.FindOthers) {
                Command command = new CommandRespondFindOthers(listenPort, sourceSocket);
                send(command);
            }
        }
        //socket.close();
    }

    public void send(Command command) {
        DatagramPacket packet = command.createDatagram();
        try {
            socket.send(packet);
            System.out.println("Sent to: " + command.getDestinationSocket()  + " : " + command.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
