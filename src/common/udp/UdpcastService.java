package common.udp;

import common.exceptions.DownloadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public abstract class UdpcastService {
    private final String udpcastRunCommand;

    protected UdpcastService(String programName, Map<String, String> params) {
        StringBuilder commandBuilder = new StringBuilder(programName);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            commandBuilder.append(" --").append(entry.getKey());

            String value = entry.getValue();
            if (!value.isEmpty()) {
                commandBuilder.append(" ").append(value);
            }
        }

        udpcastRunCommand = commandBuilder.toString();
    }

    public void processFile(Path filePath) throws DownloadException {
        String command = udpcastRunCommand + " --file " + filePath.toString();
        // TODO: "/bin/bash", "-c" - is needed?
        ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList("/bin/bash", "-c", command));
        Process process = null;
        try {
            process = processBuilder.start();

            readOutput(process.getInputStream());
            readOutput(process.getErrorStream());

            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
            if (exitCode != 0) {
                throw new IOException("Error: exit code=" + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            if (process != null && process.isAlive()) {
                process.destroy();
            }

            throw new DownloadException(e, "Could not send/receive file: " + filePath);
        }
    }

    private void readOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
