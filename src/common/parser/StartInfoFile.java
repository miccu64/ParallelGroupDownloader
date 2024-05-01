package common.parser;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class StartInfoFile extends InfoFile {
    public final Path filePath;

    public final String url;
    public final String fileName;
    public final long fileSizeInMB;

    private final String separator = "_#!@%&#_";

    public StartInfoFile(String saveDirectory, String url, String fileName, long fileSizeInMB) throws DownloadException {
        this.url = url;
        this.fileName = fileName;
        this.fileSizeInMB = fileSizeInMB;

        filePath = Paths.get(saveDirectory, "startInfo.txt");
        saveToFile(filePath);
    }

    public StartInfoFile(Path filePath) throws DownloadException {
        this.filePath = filePath;
        String errorText = this.errorText + filePath;
        try {
            List<String> values = tryGetInfo(filePath, separator);
            if (values.size() != 3) {
                throw new DownloadException(errorText);
            }

            this.url = values.get(0);
            this.fileName = values.get(1);
            this.fileSizeInMB = Integer.parseInt(values.get(2));
        } catch (InfoFileException | NumberFormatException e) {
            throw new DownloadException(e, errorText);
        }
    }

    @Override
    public String toString() {
        return separator +
                url + separator +
                fileName + separator +
                fileSizeInMB + separator;
    }
}
