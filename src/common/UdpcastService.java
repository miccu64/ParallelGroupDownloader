package common;

import common.exceptions.DownloadException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class UdpcastService {
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    private final String udpcastRunCommand;
    private final File udpcastPath;

    protected UdpcastService(String programName, Map<String, String> params, String udpcastPath) throws DownloadException {
        if (isWindows) {
            programName += ".exe";
        }

        if (udpcastPath != null) {
            Path executablePath = Paths.get(udpcastPath, programName);
            if (!Files.exists(executablePath)) {
                throw new DownloadException("UDPcast executable does not exist in: " + executablePath);
            }
            this.udpcastPath = Paths.get(udpcastPath).toFile();
        } else {
            this.udpcastPath = null;
        }

        checkUdpcastPacketPresence(programName);

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
        String command = udpcastRunCommand + " --file \"" + filePath.toAbsolutePath() + "\"";
        ProcessBuilder processBuilder = prepareProcessBuilder(command);
        Process process = null;
        try {
            process = processBuilder.start();
            getProcessOutput(process);

            process.waitFor(1, TimeUnit.SECONDS);
            int exitCode = process.exitValue();
            System.out.println("Process exited with code: " + exitCode);
            if (exitCode != 0) {
                throw new IOException("Error: exit code=" + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new DownloadException(e, "Could not send/receive file: " + filePath.toAbsolutePath());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private ProcessBuilder prepareProcessBuilder(String command) {
        List<String> params;
        if (isWindows) {
            params = Arrays.asList("cmd.exe", "/c", command);
        } else {
            params = Arrays.asList("/bin/bash", "-c", command);
        }

        return new ProcessBuilder(params).redirectErrorStream(true)
                .directory(this.udpcastPath);
    }

    private void getProcessOutput(Process process) throws IOException {
        try (InputStream is = process.getInputStream();
             InputStreamReader isReader = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                System.out.flush();
            }
        }
    }

    private void checkUdpcastPacketPresence(String programName) throws DownloadException {
        String errorText = "UDPcast packet is not present in system or given directory. "
                + "Linux: install via 'sudo apt install udpcast'. "
                + "Windows: download it from https://www.udpcast.linux.lu/";
        try {
            Process process = prepareProcessBuilder(programName + " --license").start();
            process.waitFor(3, TimeUnit.SECONDS);
            int result = process.exitValue();
            if (result != 0) {
                throw new DownloadException(errorText);
            }
        } catch (IOException | InterruptedException | IllegalThreadStateException e) {
            throw new DownloadException(e, errorText);
        }
    }
}
