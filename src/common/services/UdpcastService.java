package common.services;

import common.exceptions.DownloadException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class UdpcastService {
    protected final int startMaxWaitInSeconds;
    protected boolean isFirstRun = true;

    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    private final String udpcastRunCommand;
    private final File udpcastPath;

    private Process process;
    private BufferedReader reader;

    protected UdpcastService(String programName, Map<String, String> params, int startMaxWaitInSeconds) throws DownloadException {
        if (startMaxWaitInSeconds < 6) {
            throw new DownloadException("Start max wait must be >= 6.");
        }
        this.startMaxWaitInSeconds = startMaxWaitInSeconds;

        String path = String.valueOf(Paths.get(System.getProperty("user.dir"), "lib/udpcast"));
        if (isWindows) {
            programName += ".exe";
            this.udpcastPath = Paths.get(path, "exe").toFile();
        } else {
            programName = "./" + programName;
            this.udpcastPath = selectProperLinuxVersion(programName, path);
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
        String command = udpcastRunCommand + " " + getVaryingProperties() + " --file \"" + filePath.toAbsolutePath() + "\"";
        ProcessBuilder processBuilder = prepareProcessBuilder(command, this.udpcastPath);
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
            stopUdpcast();
            throw new DownloadException(e, "Could not send/receive file: " + filePath.toAbsolutePath());
        }
    }

    public void stopUdpcast() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }

        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    protected String getVaryingProperties() {
        if (isFirstRun) {
            isFirstRun = false;
            return "--start-timeout " + startMaxWaitInSeconds;
        } else {
            return "--start-timeout 30";
        }
    }

    private ProcessBuilder prepareProcessBuilder(String command, File udpcastPath) {
        List<String> params;
        if (isWindows) {
            params = Arrays.asList("cmd.exe", "/c", command);
        } else {
            params = Arrays.asList("/bin/bash", "-c", command);
        }

        return new ProcessBuilder(params).redirectErrorStream(true).directory(udpcastPath);
    }

    private void getProcessOutput(Process process) throws IOException {
        try (InputStream is = process.getInputStream();
             InputStreamReader isReader = new InputStreamReader(is)) {
            reader = new BufferedReader(isReader);

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    System.out.flush();
                }
            } catch (IOException ignored) {
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private File selectProperLinuxVersion(String programName, String path) throws DownloadException {
        List<String> versions = Arrays.asList("deb-x64", "rpm-x64", "deb-x86", "rpm-x86");
        for (String version : versions) {
            try {
                Path versionPath = Paths.get(path, version, "sbin");
                Process process = prepareProcessBuilder(programName + " --license", versionPath.toFile()).start();
                process.waitFor(1, TimeUnit.SECONDS);
                int result = process.exitValue();
                if (result == 0) {
                    return versionPath.toFile();
                }
            } catch (IOException | InterruptedException | IllegalThreadStateException ignored) {
            }
        }

        throw new DownloadException("Cannot run UDPcast library.");
    }
}
