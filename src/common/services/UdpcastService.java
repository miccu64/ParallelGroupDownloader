package common.services;

import common.exceptions.DownloadException;
import common.models.UdpcastConfiguration;
import common.utils.FilePartUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class UdpcastService {
    protected Process process;
    private final List<String> runParams;
    private long downloadSizeInBytes = 0;
    private long remainingSizeInBytes = 0;
    private long startTime;

    protected UdpcastService(String programName, UdpcastConfiguration configuration, List<String> params) throws DownloadException {
        String executablePath;
        if (FilePartUtils.isWindows) {
            executablePath = extractExecutable("/udpcast/exe/" + programName + ".exe", programName);
        } else {
            executablePath = selectProperLinuxVersion(programName);
        }

        params.add(0, executablePath);
        params.add("--nokbd");
        params.add("--portbase");
        params.add(String.valueOf(configuration.getPortbase()));
        params.add("--stat-period");
        params.add("1000");
        if (configuration.getNetworkInterface() != null) {
            params.add("--interface");
            params.add(configuration.getNetworkInterface());
        }

        runParams = params;
    }

    public void processFile(Path filePath) throws DownloadException {
        FilePartUtils.markToDeleteOnExit(filePath);

        List<String> params = new ArrayList<>(runParams);
        params.add("--file");
        params.add(filePath.toAbsolutePath().toString());

        ProcessBuilder processBuilder = new ProcessBuilder(params).redirectErrorStream(true);
        try {
            System.out.println("UDPcast - processing file part: " + filePath.toAbsolutePath());
            process = processBuilder.start();
            getProcessOutput(process);

            process.waitFor(1, TimeUnit.SECONDS);
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new IOException("Error: exit code=" + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            stopUdpcast();
            FilePartUtils.removeFile(filePath);
            throw new DownloadException(e, "Could not send/receive file: " + filePath.toAbsolutePath());
        }
    }

    public void setDownloadSize(int downloadSizeInMB) {
        this.downloadSizeInBytes = FilePartUtils.megabytesToBytes(downloadSizeInMB);
        this.remainingSizeInBytes = this.downloadSizeInBytes;
        this.startTime = System.nanoTime();
    }

    public void stopUdpcast() {
        if (process != null) {
            process.destroyForcibly();
        }
    }

    private void getProcessOutput(Process process) throws IOException {
        try (InputStream is = process.getInputStream();
             InputStreamReader isReader = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isReader)) {
            String speedLineStart = "bytes=";
            long bytes;
            long latestBytes = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(speedLineStart)) {
                    if (line.startsWith("Timeout")) {
                        System.out.print("Lost connection with one of clients - waiting...");
                    } else if (line.startsWith("Dropping client")) {
                        System.out.println("Dropped one of clients");
                    }

                    continue;
                }

                bytes = parseBytes(line.substring(speedLineStart.length()));
                long currentBytes = bytes - latestBytes;
                latestBytes = bytes;
                remainingSizeInBytes -= currentBytes;

                printDownloadInfo(currentBytes);
            }
        }
    }

    private long parseBytes(String text) {
        StringBuilder result = new StringBuilder();
        for (char character : text.toCharArray()) {
            boolean isDigit = Character.isDigit(character);
            boolean isSpace = character == ' ';
            if (isDigit) {
                result.append(character);
            } else if (!isSpace) {
                break;
            }
        }

        return Long.parseLong(result.toString());
    }

    private void printDownloadInfo(long currentBytes) {
        int speedMBps = FilePartUtils.bytesToMegabytes(currentBytes);
        String outText = "Speed: " + speedMBps + "MBps, estimated time left: ";
        if (remainingSizeInBytes > 0) {
            long secondsElapsed = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
            long megabytesDownloaded = FilePartUtils.bytesToMegabytes(downloadSizeInBytes - remainingSizeInBytes);
            double ratioMBps = (double) megabytesDownloaded / secondsElapsed;
            double estimatedTime = FilePartUtils.bytesToMegabytes(remainingSizeInBytes) / ratioMBps;
            int estimatedMinutes = (int) (estimatedTime / 60);
            int estimatedSeconds = (int) estimatedTime - estimatedMinutes * 60;

            outText += estimatedMinutes + " minutes " + estimatedSeconds + " seconds";
        } else {
            outText += "n/a";
        }
        System.out.println(outText);
    }

    private String selectProperLinuxVersion(String programName) throws DownloadException {
        List<String> versions = Arrays.asList("deb-x64", "rpm-x64", "deb-x86", "rpm-x86");
        for (String version : versions) {
            try {
                String executableUrl = extractExecutable("/udpcast/" + version + "/" + programName, programName);
                Process process = new ProcessBuilder(executableUrl, "--license").redirectErrorStream(true).start();
                process.waitFor(1, TimeUnit.SECONDS);
                if (process.exitValue() == 0) {
                    return executableUrl;
                }
            } catch (IOException | InterruptedException | IllegalThreadStateException ignored) {
            }
        }

        throw new DownloadException("Cannot run udpcast library. Missing GLIBC library " +
                "(required min version 2.34 - try install via 'sudo apt install libc6') or not supported OS.");
    }

    private String extractExecutable(String resourcePath, String programName) throws DownloadException {
        URL executableUrl = UdpcastService.class.getResource(resourcePath);
        if (executableUrl == null) {
            throw new DownloadException("Could not load udpcast packet files from resources.");
        }

        try {
            String extension = FilePartUtils.isWindows ? ".exe" : "";
            File file = File.createTempFile(programName, extension);
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
            throw new DownloadException("Could not load udpcast packet files from resources.");
        }
    }
}
