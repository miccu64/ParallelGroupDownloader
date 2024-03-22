package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

public abstract class UdpcastWrapper implements Runnable, AutoCloseable  {
    private final ProcessBuilder processBuilder;
    private Process process;

    protected UdpcastWrapper(String programName, Map<String, String> params){
        StringBuilder command = new StringBuilder(programName);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            command.append(" --").append(entry.getKey());

            String value = entry.getValue();
            if (!value.isEmpty()) {
                command.append(" ").append(value);
            }
        }

        this.processBuilder = new ProcessBuilder(Arrays.asList("/bin/bash", "-c", command.toString()));
    }

    @Override
    public void run() {
        try {
            process = processBuilder.start();

            // Read standard output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Read error output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        if (process != null && process.isAlive()){
            process.destroy();
        }
    }
}
