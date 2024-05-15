package common.infos;

import common.exceptions.DownloadException;
import common.exceptions.InfoFileException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EndInfoFile extends InfoFile {
    public final Path filePath;

    private final List<String> checksums;
    private final String separator = "#!_@%&_#";

    public List<String> getChecksums() {
        return checksums;
    }

    public EndInfoFile(String saveDirectory, List<String> checksums) throws DownloadException {
        if (checksums == null || checksums.isEmpty()) {
            throw new DownloadException("No checksums are given.");
        }
        if (saveDirectory == null || saveDirectory.isEmpty() || !Files.exists(Paths.get(saveDirectory))) {
            throw new DownloadException("Improper save directory.");
        }

        this.checksums = checksums;

        filePath = Paths.get(saveDirectory, "endInfo.txt");
        saveToFile(filePath);
    }

    public EndInfoFile(Path filePath) throws DownloadException, InfoFileException {
        this.filePath = filePath;

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
