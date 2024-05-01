package common.parser;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;

import java.nio.file.Path;
import java.util.List;

public class EndInfoFile extends InfoFile {
    private final List<String> checksums;
    private final String separator = "#!_@%&_#";

    public List<String> getChecksums() {
        return checksums;
    }

    public EndInfoFile(List<String> checksums) throws DownloadException {
        if (checksums == null || checksums.isEmpty()) {
            throw new DownloadException("No checksums are given.");
        }

        this.checksums = checksums;
    }

    public EndInfoFile(Path filePath) throws DownloadException, InfoFileException {
        List<String> values = tryGetInfo(filePath, separator);
        if (values.isEmpty()) {
            throw new InfoFileException(this.errorText + filePath);
        }

        this.checksums = values;
    }

    @Override
    public String toString() {
        return separator + String.join(separator, checksums) + separator;
    }
}
