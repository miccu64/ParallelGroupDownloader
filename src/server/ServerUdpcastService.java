package server;

import common.models.UdpcastConfiguration;
import common.exceptions.DownloadException;
import common.services.UdpcastService;

import java.nio.file.Path;
import java.util.HashMap;

public class ServerUdpcastService extends UdpcastService {
    public ServerUdpcastService(UdpcastConfiguration configuration) throws DownloadException {
        super("udp-sender", new HashMap<String, String>() {{
            put("nokbd", "");
            put("min-wait", "3");
            put("retries-until-drop", "30");
            put("portbase", String.valueOf(configuration.getPortbase()));

            int thirtyMinutesAsSeconds = 30 * 60;
            put("start-timeout", String.valueOf(thirtyMinutesAsSeconds));

            if (configuration.getNetworkInterface() != null) {
                put("interface", configuration.getNetworkInterface());
            }
        }});
    }

    @Override
    public void processFile(Path filePath) throws DownloadException {
        super.processFile(filePath);

        sleepOneSecond();
    }

    private void sleepOneSecond() {
        // udp-receiver has by default 500 delay in closing (--exit-wait parameter)
        // receiver waits 500ms after receiving the final REQACK in order to guard against loss of the final ACK
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
