package client;

import common.UdpcastWrapper;

import java.util.HashMap;

public class ClientUdpcastWrapper extends UdpcastWrapper {
    public ClientUdpcastWrapper(int port){
        super("udp-receiver", new HashMap<String, String>() {{
            put("nokbd", "");
            put("start-timeout", "3");
            put("portbase", String.valueOf(port));
        }});
    }
}
