package common.infos;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;
import common.utils.FilePartUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class InfoFile {
    protected final String errorText = "Given file is not proper start/end file. Path: ";

    protected void saveToFile(Path filePath) throws DownloadException {
        try {
            FilePartUtils.markToDeleteOnExit(filePath);
            Files.write(filePath, toString().getBytes());
        } catch (IOException e) {
            throw new DownloadException(e, "Could not save file: " + filePath);
        }
    }

    protected List<String> tryGetInfo(Path filePath, String separator) throws DownloadException, InfoFileException {
        try {
            long fileSizeInBytes = Files.size(filePath);
            if (fileSizeInBytes > 1000000) {
                throw new InfoFileException("Improper file size.");
            }

            long lineCount;
            try (Stream<String> stream = Files.lines(filePath, StandardCharsets.ISO_8859_1)) {
                lineCount = stream.count();
            }
            if (lineCount != 1) {
                throw new InfoFileException("Too much lines.");
            }

            String line = Files.readAllLines(filePath).get(0);
            if (!line.startsWith(separator) || !line.endsWith(separator)) {
                throw new InfoFileException("Improper file format.");
            }

            return Arrays.stream(line.split(separator))
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException | IndexOutOfBoundsException e) {
            throw new DownloadException(e, errorText);
        }
    }
}
