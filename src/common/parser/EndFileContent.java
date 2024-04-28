package common.parser;

import common.DownloadException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class EndFileContent {
    private final List<String> checksums;
    private final String separator = "#!@%&*|#";

    public List<String> getChecksums() {
        return checksums;
    }

    public EndFileContent(List<String> checksums) throws DownloadException {
        if (checksums == null || checksums.isEmpty()) {
            throw new DownloadException("No checksums are given.");
        }

        this.checksums = checksums;
    }

    public EndFileContent(Path filePath) throws DownloadException {
        String errorText = "Given file is not proper end file. Path: " + filePath;
        try {
            long fileSizeInBytes = Files.size(filePath);
            if (fileSizeInBytes > 1000) {
                throw new DownloadException(errorText);
            }

            List<String> lines = Files.readAllLines(filePath);
            if (lines.size() != 1) {
                throw new DownloadException(errorText);
            }

            String[] checksums = lines.get(0).split(separator);
            if (checksums.length < 1) {
                throw new DownloadException(errorText);
            }

            this.checksums = Arrays.asList(checksums);
        } catch (IOException e) {
            throw new DownloadException(e, errorText);
        }
    }

    @Override
    public String toString() {
        return String.join(separator, checksums);
    }
}
