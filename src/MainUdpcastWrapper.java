import client.udp.ClientUdpcastService;
import common.udp.FileInfoHolder;
import server.udp.ServerUdpcastService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MainUdpcastWrapper {
    public static void main(String[] args) {
        int port = 10106;
        ClientUdpcastService c = new ClientUdpcastService(port, new FileInfoHolder());
        ServerUdpcastService s = new ServerUdpcastService(port, new FileInfoHolder());
        List<Callable<Integer>> callableTasks = new ArrayList<>();
        callableTasks.add(c);
        callableTasks.add(s);
    }
}
