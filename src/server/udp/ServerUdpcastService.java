package server.udp;

import common.udp.FileInfoHolder;
import common.udp.UdpcastService;

import java.util.HashMap;

public class ServerUdpcastService extends UdpcastService {
    public ServerUdpcastService(int port, FileInfoHolder fileInfoHolder) {
        super("udp-sender", new HashMap<String, String>() {{
            put("nokbd", "");
            put("max-wait", "10");
            put("min-receivers", "1");
            put("portbase", String.valueOf(port));
        }}, fileInfoHolder);
    }
}
