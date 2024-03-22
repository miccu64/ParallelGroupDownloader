package server;

import common.UdpcastWrapper;

import java.util.HashMap;

public class ServerUdpcastWrapper extends UdpcastWrapper {
    public ServerUdpcastWrapper(int port, String file){
        super("udp-sender", new HashMap<String, String>() {{
            put("nokbd", "");
            put("max-wait", "10");
            put("min-receivers", "1");
            put("portbase", String.valueOf(port));
            put("file", file);
        }});
    }
}
