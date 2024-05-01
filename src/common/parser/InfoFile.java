package common.parser;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class InfoFile {
    protected final String errorText = "Given file is not proper end file. Path: ";

    protected void saveToFile(Path filePath) throws DownloadException {
        try {
            Files.write(filePath, toString().getBytes());
        } catch (IOException e) {
            throw new DownloadException(e, "Could not save file: " + filePath);
        }
    }

    protected List<String> tryGetInfo(Path filePath, String separator) throws DownloadException, InfoFileException {
        try {
            long fileSizeInBytes = Files.size(filePath);
            int oneMBAsBytes = 1000000;
            if (fileSizeInBytes > oneMBAsBytes) {
                throw new InfoFileException("Improper file size.");
            }

            List<String> lines = Files.readAllLines(filePath);
            if (lines.size() != 1) {
                throw new InfoFileException("Too much lines.");
            }

            String line = lines.get(0);
            if (!line.startsWith(separator) || !line.endsWith(separator)) {
                throw new InfoFileException("Improper file format.");
            }

            return Arrays.stream(line.split(separator))
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new DownloadException(e, errorText);
        }
    }
}
