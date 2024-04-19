package client.udp;

import common.udp.FileInfoHolder;
import common.udp.UdpcastService;

import java.util.HashMap;

public class ClientUdpcastService extends UdpcastService {
    public ClientUdpcastService(int port, FileInfoHolder fileInfoHolder) {
        super("udp-receiver", new HashMap<String, String>() {{
            put("nokbd", "");
            put("start-timeout", "3");
            put("receive-timeout", "10");
            put("portbase", String.valueOf(port));
        }}, fileInfoHolder);
    }
}
