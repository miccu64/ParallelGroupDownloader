import client.ClientUdpcastService;
import server.ServerUdpcastService;

public class MainUdpcastWrapper {
    public static void main(String[] args) {
        int port = 10106;
        ClientUdpcastService c = new ClientUdpcastService(port);
        ServerUdpcastService s = new ServerUdpcastService(port);
    }
}
