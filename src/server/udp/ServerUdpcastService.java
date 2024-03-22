package server.udp;

import common.udp.UdpcastService;

import java.util.HashMap;

public class ServerUdpcastService extends UdpcastService {
    public ServerUdpcastService(int port, String file){
        super("udp-sender", new HashMap<String, String>() {{
            put("nokbd", "");
            put("max-wait", "10");
            put("min-receivers", "1");
            put("portbase", String.valueOf(port));
            put("file", file);
        }});
    }
}
