package common.udp;

import common.DownloadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class UdpcastService implements Runnable {
    public final AtomicBoolean downloadInProgress = new AtomicBoolean(true);

    private final StringBuilder udpcastRunCommand;
    private final FileInfoHolder fileInfoHolder;

    protected UdpcastService(String programName, Map<String, String> params, FileInfoHolder fileInfoHolder) {
        udpcastRunCommand = new StringBuilder(programName);
        this.fileInfoHolder = fileInfoHolder;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            udpcastRunCommand.append(" --").append(entry.getKey());

            String value = entry.getValue();
            if (!value.isEmpty()) {
                udpcastRunCommand.append(" ").append(value);
            }
        }
    }

    @Override
    public void run() {
        while (downloadInProgress.get()) {
            Path path = fileInfoHolder.filesToProcess.peek();
            if (path == null) {
                try {
                    wait(1000);
                } catch (InterruptedException ignored) {
                }
            } else {
                try {
                    processSingleFile(path);
                } catch (DownloadException e) {
                    // TODO: handle status
                    downloadInProgress.set(false);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void processSingleFile(Path path) throws DownloadException {
        String command = udpcastRunCommand.toString() + " --file " + path.toString();
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

            fileInfoHolder.processedFiles.add(path);
            fileInfoHolder.filesToProcess.remove(path);
        } catch (IOException | InterruptedException e) {
            if (process != null && process.isAlive()) {
                process.destroy();
            }

            throw new DownloadException(e, "Could not send/receive file: " + path);
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
