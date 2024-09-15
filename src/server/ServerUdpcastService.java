package server;

import common.exceptions.DownloadException;
import common.infos.EndInfoFile;
import common.models.UdpcastConfiguration;
import common.services.UdpcastService;
import common.utils.VariousUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ServerUdpcastService extends UdpcastService {
    private int expectedClients = 0;

    public ServerUdpcastService(UdpcastConfiguration configuration) throws DownloadException {
        super("udp-sender", configuration,
                new ArrayList<>(Arrays.asList(
                        "--min-wait", "1",
                        "--full-duplex"
                )));
    }

    @Override
    public void processFile(Path filePath) throws DownloadException {System.out.println("Server processFile");
        ArrayList<String> additionalParams = new ArrayList<>();
        if (expectedClients > 0) {
            additionalParams.add("--min-receivers");
            additionalParams.add(String.valueOf(expectedClients));
            additionalParams.add("--start-timeout");
            additionalParams.add("15");
            additionalParams.add("--max-wait");
            additionalParams.add("10");

            System.out.println("Expected clients: " + expectedClients);
        }
        expectedClients = 0;
        super.processFile(filePath, additionalParams);

        // udp-receiver has by default 500 delay in closing (--exit-wait parameter)
        // receiver waits 500ms after receiving the final REQACK in order to guard against loss of the final ACK
        VariousUtils.sleep(1);
    }

    public void shutdownClients() {
        System.out.println("shutdownClients");
        if (process != null) {
            System.out.println("process != null");
            if (process.isAlive()) {
                super.stopUdpcast();
                System.out.println("stopUdpcast");
            }

            try {
                if (!process.waitFor(1, TimeUnit.SECONDS)) {
                    System.out.println("waitFor failed");
                    return;
                }
                System.out.println("waitFor success");

                Path tempDir = Files.createTempDirectory(null);
                EndInfoFile endInfoFile = new EndInfoFile(String.valueOf(tempDir), Collections.singletonList(EndInfoFile.errorContent));
                ArrayList<String> additionalParams = new ArrayList<>();
                additionalParams.add("--start-timeout");
                additionalParams.add("1");
                additionalParams.add("--max-wait");
                additionalParams.add("1");

                super.processFile(endInfoFile.filePath, additionalParams);
            } catch (DownloadException | IOException | InterruptedException ignored) {
            }
        }
    }

    @Override
    protected long processLine(String line, long latestBytes) {
        if (line.startsWith("Dropping one of clients")) {
            System.out.println("Dropped one of clients");
            expectedClients -= 1;
        } else if (line.startsWith("New connection from")) {
            expectedClients += 1;
        }

        return super.processLine(line, latestBytes);
    }
}
