package client;

import common.models.UdpcastConfiguration;
import common.exceptions.DownloadException;
import common.services.UdpcastService;

import java.util.HashMap;

public class ClientUdpcastService extends UdpcastService {
    public ClientUdpcastService(UdpcastConfiguration configuration) throws DownloadException {
        super("udp-receiver", new HashMap<String, String>() {{
            put("nokbd", "");
            put("receive-timeout", "30");
            put("portbase", String.valueOf(configuration.getPortbase()));

            int thirtyMinutesAsSeconds = 30 * 60;
            put("start-timeout", String.valueOf(thirtyMinutesAsSeconds));

            if (configuration.getNetworkInterface() != null) {
                put("interface", configuration.getNetworkInterface());
            }
        }});
    }
}
