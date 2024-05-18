package server;

import common.exceptions.DownloadException;
import common.models.UdpcastConfiguration;
import common.services.UdpcastService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerUdpcastService extends UdpcastService {
    public ServerUdpcastService(UdpcastConfiguration configuration) throws DownloadException {
        super("udp-sender", configuration,
                new ArrayList<>(Arrays.asList(
                        "--min-wait", "3",
                        "--start-timeout", "5"
                )));
    }

    @Override
    public void processFile(Path filePath) throws DownloadException {
        super.processFile(filePath);

        waitForReceiversFinalize();
    }

    private void waitForReceiversFinalize() {
        // udp-receiver has by default 500 delay in closing (--exit-wait parameter)
        // receiver waits 500ms after receiving the final REQACK in order to guard against loss of the final ACK
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
