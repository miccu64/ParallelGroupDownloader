package client;

import common.UdpcastConfiguration;
import common.exceptions.DownloadException;
import common.services.UdpcastService;

import java.util.HashMap;

public class ClientUdpcastService extends UdpcastService {
    public ClientUdpcastService(UdpcastConfiguration configuration) throws DownloadException {
        super("udp-receiver", new HashMap<String, String>() {{
            put("nokbd", "");
            put("receive-timeout", "30");
            put("portbase", String.valueOf(configuration.portbase));

            if (configuration.networkInterface != null) {
                put("interface", configuration.networkInterface);
            }
        }}, configuration.startMaxWaitInSeconds);
    }
}
