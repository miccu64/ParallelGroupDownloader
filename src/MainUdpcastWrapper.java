import client.udp.ClientUdpcastService;
import server.udp.ServerUdpcastService;

public class MainUdpcastWrapper {
    public static void main(String[] args) {
        int port = 10106;
        ClientUdpcastService c = new ClientUdpcastService(port);
        ServerUdpcastService s = new ServerUdpcastService(port, "/home/lubuntu/Desktop/someFile.txt");
        Thread t1 = new Thread(c);
        Thread t2 = new Thread(s);
        t1.start();
        t2.start();
    }
}
