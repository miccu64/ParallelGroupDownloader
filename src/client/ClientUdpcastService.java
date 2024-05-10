package client;

import common.exceptions.DownloadException;
import common.services.UdpcastService;

import java.util.HashMap;

public class ClientUdpcastService extends UdpcastService {
    public ClientUdpcastService(int port) throws DownloadException {
        super("udp-receiver", new HashMap<String, String>() {{
            put("nokbd", "");
            put("start-timeout", "300");
            put("receive-timeout", "10");
            put("portbase", String.valueOf(port));
        }});
    }
}
