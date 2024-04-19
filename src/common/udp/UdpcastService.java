package common.udp;

import common.DownloadException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static common.utils.FilePartUtils.joinAndDeleteFileParts;

public abstract class UdpcastService implements Runnable {
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
        while (fileInfoHolder.isInProgress()) {
            Path path = fileInfoHolder.filesToProcess.peek();
            if (path == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            } else {
                try {
                    processSingleFile(path);

                    if (fileInfoHolder.processedFiles.size() == fileInfoHolder.expectedPartsCount.get()){
                        if (joinAndDeleteFileParts(new ArrayList<>(fileInfoHolder.processedFiles))) {
                            fileInfoHolder.setSuccessStatus();
                        } else {
                            fileInfoHolder.setErrorStatus();
                        }
                    }
                } catch (DownloadException e) {
                    fileInfoHolder.setErrorStatus();
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
