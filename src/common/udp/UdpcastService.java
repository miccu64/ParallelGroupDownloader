package common.udp;

import common.exceptions.DownloadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class UdpcastService {
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    private final String udpcastRunCommand;

    protected UdpcastService(String programName, Map<String, String> params, String udpcastPath) throws DownloadException {
        if (isWindows) {
            programName += ".exe";
        }
        if (udpcastPath != null) {
            Path path = Paths.get(udpcastPath, programName);
            if (!Files.exists(path)) {
                throw new DownloadException("UDPcast executable does not exist in: " + path);
            }
            programName = String.valueOf(path);
        }

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
        String command = udpcastRunCommand + " --file " + filePath.toAbsolutePath();
        List<String> params;
        if (isWindows) {
            params = Arrays.asList("cmd.exe", "/c", command);
        } else {
            params = Arrays.asList("/bin/bash", "-c", command);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(params);
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

            throw new DownloadException(e, "Could not send/receive file: " + filePath.toAbsolutePath());
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
