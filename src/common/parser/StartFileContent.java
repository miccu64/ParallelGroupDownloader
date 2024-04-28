package common.parser;

import common.DownloadException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StartFileContent {
    public final String url;
    public final String fileName;
    public final long fileSizeInMB;

    private final String separator = "#!@%&*|#";

    public StartFileContent(String url, String fileName, long fileSizeInMB) {
        this.url = url;
        this.fileName = fileName;
        this.fileSizeInMB = fileSizeInMB;
    }

    public StartFileContent(Path filePath) throws DownloadException {
        String errorText = "Given file is not proper start file. Path: " + filePath;
        try {
            long fileSizeInBytes = Files.size(filePath);
            if (fileSizeInBytes > 1000) {
                throw new DownloadException(errorText);
            }

            List<String> lines = Files.readAllLines(filePath);
            if (lines.size() != 1) {
                throw new DownloadException(errorText);
            }

            String[] values = lines.get(0).split(separator);
            if (values.length != 3) {
                throw new DownloadException(errorText);
            }

            this.url = values[0];
            this.fileName = values[1];
            this.fileSizeInMB = Integer.parseInt(values[2]);
        } catch (IOException e) {
            throw new DownloadException(e, errorText);
        }
    }

    @Override
    public String toString() {
        return url + separator +
                fileName + separator +
                fileSizeInMB;
    }
}
