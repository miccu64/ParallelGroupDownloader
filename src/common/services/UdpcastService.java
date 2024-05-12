package common.services;

import common.exceptions.DownloadException;
import common.models.UdpcastConfiguration;
import common.utils.FilePartUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class UdpcastService {
    private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    private final List<String> runParams;
    private Process process;

    protected UdpcastService(String programName, UdpcastConfiguration configuration, List<String> params) throws DownloadException {
        String executablePath;
        if (isWindows) {
            URL resourceUrl = getResource("/udpcast/exe/" + programName + ".exe");
            executablePath = getExecutable(resourceUrl, programName);
        } else {
            URL resourceUrl = selectProperLinuxVersion(programName);
            executablePath = "./" + getExecutable(resourceUrl, programName);
        }

        params.add(0, executablePath);
        params.add("--nokbd");
        params.add("--portbase");
        params.add(String.valueOf(configuration.getPortbase()));
        if (configuration.getNetworkInterface() != null) {
            params.add("--interface");
            params.add(configuration.getNetworkInterface());
        }

        runParams = params;
    }

    public void processFile(Path filePath) throws DownloadException {
        List<String> params = new ArrayList<>(runParams);
        params.add("--file");
        params.add(filePath.toAbsolutePath().toString());

        filePath.toFile().deleteOnExit();

        ProcessBuilder processBuilder = new ProcessBuilder(params).redirectErrorStream(true);
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
        if (process != null) {
            process.destroy();
            try {
                if (!process.waitFor(1, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException ignored) {
                process.destroyForcibly();
            }
        }
    }

    private void getProcessOutput(Process process) throws IOException {
        try (InputStream is = process.getInputStream();
             InputStreamReader isReader = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isReader)) {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    System.out.flush();
                }
            } catch (IOException ignored) {
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

    private URL selectProperLinuxVersion(String programName) throws DownloadException {
        List<String> versions = Arrays.asList("deb-x64", "rpm-x64", "deb-x86", "rpm-x86");
        for (String version : versions) {
            try {
                URL executableUrl = getResource("/udpcast/" + version + "/sbin/" + programName);
                Process process = new ProcessBuilder(executableUrl.getPath(), "--license").redirectErrorStream(true).start();
                process.waitFor(1, TimeUnit.SECONDS);
                int result = process.exitValue();
                if (result == 0) {
                    return executableUrl;
                }
                return null;
            } catch (IOException | InterruptedException | IllegalThreadStateException ignored) {
            }
        }

        throw new DownloadException("Cannot run UDPcast library.");
    }

    private String getExecutable(URL executableUrl, String programName) throws DownloadException {
        try {
            String extension = isWindows ? ".exe" : "";
            File file;
            try {
                Path path = Paths.get(programName + extension);
                FilePartUtils.removeFile(path);
                file = Files.createFile(path).toFile();
            } catch (IOException e) {
                file = File.createTempFile(programName, extension);
            }

            boolean ignored = file.setExecutable(true);
            file.deleteOnExit();

            try (FileOutputStream outputStream = new FileOutputStream(file);
                 InputStream inputStream = executableUrl.openStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }
            return file.getPath();
        } catch (IOException e) {
            throw new DownloadException("Could not load UDPcast packet files.");
        }
    }
}
