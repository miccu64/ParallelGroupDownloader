package common.parser;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;

import java.nio.file.Path;
import java.util.List;

public class StartInfoFile extends InfoFile {
    public final String url;
    public final String fileName;
    public final long fileSizeInMB;

    private final String separator = "_#!@%&#_";

    public StartInfoFile(String url, String fileName, long fileSizeInMB) {
        this.url = url;
        this.fileName = fileName;
        this.fileSizeInMB = fileSizeInMB;
    }

    public StartInfoFile(Path filePath) throws DownloadException {
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
