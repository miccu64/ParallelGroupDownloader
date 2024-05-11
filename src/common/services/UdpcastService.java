package common.services;

import common.exceptions.DownloadException;
import common.utils.FilePartUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class UdpcastService {
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    private final String udpcastRunCommand;

    private Process process;
    private BufferedReader reader;

    protected UdpcastService(String programName, Map<String, String> params) throws DownloadException {
        String runCommand;
        if (isWindows) {
            programName += ".exe";
            runCommand = getExecutable(getResource("/udpcast/exe/" + programName));
        } else {
            runCommand = selectProperLinuxVersion(programName);
        }

        StringBuilder commandBuilder = new StringBuilder(runCommand);
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
        String command = udpcastRunCommand + " " + " --file \"" + filePath.toAbsolutePath() + "\"";
        ProcessBuilder processBuilder = prepareProcessBuilder(command);
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
            FilePartUtils.removeFile(filePath);
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

        if (process != null) {
            process.destroy();
            process.destroyForcibly();
        }
    }

    private ProcessBuilder prepareProcessBuilder(String command) {
        List<String> params;
        if (isWindows) {
            params = Arrays.asList("cmd.exe", "/c", command);
        } else {
            params = Arrays.asList("/bin/bash", "-c", command);
        }

        return new ProcessBuilder(params).redirectErrorStream(true);
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

    private URL getResource(String name) throws DownloadException {
        URL url = UdpcastService.class.getResource(name);
        if (url == null) {
            throw new DownloadException("Could not load UDPcast packet files.");
        }
        return url;
    }

    private String selectProperLinuxVersion(String programName) throws DownloadException {
        List<String> versions = Arrays.asList("deb-x64", "rpm-x64", "deb-x86", "rpm-x86");
        for (String version : versions) {
            try {
                URL executableUrl = getResource("/udpcast/" + version + "/sbin/" + programName);
                String executable = "./" + getExecutable(executableUrl);
                Process process = prepareProcessBuilder(executable + " --license").start();
                process.waitFor(1, TimeUnit.SECONDS);
                int result = process.exitValue();
                if (result == 0) {
                    return executable;
                }
            } catch (IOException | InterruptedException | IllegalThreadStateException ignored) {
            }
        }

        throw new DownloadException("Cannot run UDPcast library.");
    }

    private String getExecutable(URL executableUrl) throws DownloadException {
        try {
            File tempFile = File.createTempFile(executableUrl.getFile(), ".exe");
            boolean ignored = tempFile.setExecutable(true);
            tempFile.deleteOnExit();

            try (FileOutputStream outputStream = new FileOutputStream(tempFile);
                 InputStream inputStream = executableUrl.openStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }
            return tempFile.getPath();
        } catch (IOException e) {
            throw new DownloadException("Could not load UDPcast packet files.");
        }
    }
}
