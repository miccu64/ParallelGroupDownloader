import client.ClientUdpcastWrapper;
import server.ServerUdpcastWrapper;

public class MainUdpcastWrapper {
    public static void main(String[] args) {
        int port = 10106;
        ClientUdpcastWrapper c = new ClientUdpcastWrapper(port);
        ServerUdpcastWrapper s = new ServerUdpcastWrapper(port, "/home/lubuntu/Desktop/someFile.txt");
        Thread t1 = new Thread(c);
        Thread t2 = new Thread(s);
        t1.start();
        t2.start();
    }
}
