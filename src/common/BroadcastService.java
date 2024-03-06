package common;

import java.io.IOException;
import java.net.*;

public class BroadcastService {
    private final int _port;

    public BroadcastService(int port)  {
        this._port = port;
    }

    public void sendBroadcast(String message) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
        byte[] data = message.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(data, data.length, broadcastAddress, _port);
        socket.send(sendPacket);
        socket.close();

        System.out.println("Sent on broadcast: " + message);
    }
}
