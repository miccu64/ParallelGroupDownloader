import client.udp.ClientUdpcastService;
import common.udp.FileInfoHolder;
import server.udp.ServerUdpcastService;

public class MainUdpcastWrapper {
    public static void main(String[] args) {
        int port = 10106;
        ClientUdpcastService c = new ClientUdpcastService(port, new FileInfoHolder());
        ServerUdpcastService s = new ServerUdpcastService(port, new FileInfoHolder());
        Thread t1 = new Thread(c);
        Thread t2 = new Thread(s);
        t1.start();
        t2.start();
    }
}
