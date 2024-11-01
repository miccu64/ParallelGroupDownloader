package common.infos;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class StartInfoFile extends InfoFile {
    public final Path filePath;

    public final String url;
    public final String fileName;
    public final int summarySizeInMB;
    public final int partSizeInMB;

    private final String separator = "_#!@%&#_";

    public StartInfoFile(String saveDirectory, String url, String fileName, int summarySizeInMB, int partSizeInMB) throws DownloadException {
        if (saveDirectory.isEmpty() || url.isEmpty() || fileName.isEmpty() || summarySizeInMB < 0 || partSizeInMB < 1) {
            throw new DownloadException("Wrong StartInfo file data.");
        }

        this.url = url;
        this.fileName = fileName;
        this.summarySizeInMB = summarySizeInMB;
        this.partSizeInMB = partSizeInMB;

        filePath = Paths.get(saveDirectory, "startInfoServer.txt");
        saveToFile(filePath);
    }

    public StartInfoFile(Path filePath) throws DownloadException {
        this.filePath = filePath;
        String errorText = this.errorText + filePath;
        try {
            List<String> values = tryGetInfo(filePath, separator);
            if (values.size() != 4) {
                throw new DownloadException(errorText);
            }

            this.url = values.get(0);
            this.fileName = values.get(1);
            this.summarySizeInMB = Integer.parseInt(values.get(2));
            this.partSizeInMB = Integer.parseInt(values.get(3));
        } catch (InfoFileException | NumberFormatException e) {
            throw new DownloadException(e, errorText);
        }
    }

    @Override
    public String toString() {
        return separator +
                url + separator +
                fileName + separator +
                summarySizeInMB + separator +
                partSizeInMB + separator;
    }
}
