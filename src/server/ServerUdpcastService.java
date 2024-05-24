package server;

import common.exceptions.DownloadException;
import common.models.UdpcastConfiguration;
import common.services.UdpcastService;
import common.utils.VariousUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerUdpcastService extends UdpcastService {
    public ServerUdpcastService(UdpcastConfiguration configuration) throws DownloadException {
        super("udp-sender", configuration,
                new ArrayList<>(Arrays.asList(
                        "--min-wait", "2",
                        "--start-timeout", "5",
                        "--full-duplex"
                )));
    }

    @Override
    public void processFile(Path filePath) throws DownloadException {
        super.processFile(filePath);

        // udp-receiver has by default 500 delay in closing (--exit-wait parameter)
        // receiver waits 500ms after receiving the final REQACK in order to guard against loss of the final ACK
        VariousUtils.sleep(1);
    }
}
