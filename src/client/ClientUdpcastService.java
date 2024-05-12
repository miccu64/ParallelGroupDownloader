package client;

import common.exceptions.DownloadException;
import common.models.UdpcastConfiguration;
import common.services.UdpcastService;

import java.util.ArrayList;
import java.util.Arrays;

public class ClientUdpcastService extends UdpcastService {
    public ClientUdpcastService(UdpcastConfiguration configuration) throws DownloadException {
        super("udp-receiver", configuration,
                new ArrayList<>(Arrays.asList(
                        "--receive-timeout", "30",
                        "--start-timeout", String.valueOf(30 * 60)
                )));
    }
}
