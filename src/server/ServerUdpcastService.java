package server;

import common.UdpcastConfiguration;
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
            put("portbase", String.valueOf(configuration.portbase));

            if (configuration.networkInterface != null) {
                put("interface", configuration.networkInterface);
            }
        }}, configuration.startMaxWaitInSeconds);
    }

    @Override
    public void processFile(Path filePath) throws DownloadException {
        super.processFile(filePath);

        sleepOneSecond();
    }

    protected String getVaryingProperties() {
        String properties;
        if (isFirstRun) {
            properties = "--min-wait " + (startMaxWaitInSeconds - 3);
        } else {
            properties = "--min-wait 3";
        }
        return super.getVaryingProperties() + " " + properties;
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
