package server;

import common.exceptions.DownloadException;
import common.services.UdpcastService;

import java.nio.file.Path;
import java.util.HashMap;

public class ServerUdpcastService extends UdpcastService {
    public ServerUdpcastService(int port) throws DownloadException {
        super("udp-sender", new HashMap<String, String>() {{
            put("nokbd", "");
            put("min-wait", "2");
            put("min-receivers", "1");
            put("portbase", String.valueOf(port));
            put("retries-until-drop", "30");
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
