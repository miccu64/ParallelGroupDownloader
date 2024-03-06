package common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UdpService extends Thread {
    private DatagramSocket socket;
    private byte[] buf = new byte[256];

    public UdpService(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    public void run() {
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String received = new String(packet.getData(), 0, packet.getLength());

            System.out.println(received);
        }
        //socket.close();
    }
}
