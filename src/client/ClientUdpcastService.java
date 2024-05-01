package client;

import common.exceptions.DownloadException;
import common.udp.UdpcastService;

import java.util.HashMap;

public class ClientUdpcastService extends UdpcastService {
    public ClientUdpcastService(int port, String udpcastPath) throws DownloadException {
        super("udp-receiver", new HashMap<String, String>() {{
            put("nokbd", "");
            put("start-timeout", "3");
            put("receive-timeout", "10");
            put("portbase", String.valueOf(port));
        }}, udpcastPath);
    }
}
