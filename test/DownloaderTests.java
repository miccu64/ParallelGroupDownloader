import common.StatusEnum;
import common.exceptions.DownloadException;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.FileDownloader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Random;

public class DownloaderTests {
    @Test
    public void testOriginalAndDownloadedFilesEquality() throws IOException, DownloadException {
        PrepareDownloadUtils.initProgram();

        String testDirectory = "filesTest";
        Files.createDirectories(Paths.get(testDirectory));
        String fileName = "testOriginalAndDownloadedFilesEquality.file";
        Path testFilePath = Paths.get(testDirectory, fileName);
        URL url = testFilePath.toUri().toURL();
        FileDownloader fileDownloader = new FileDownloader(url.toString(), 1);

        byte[] bytes = new byte[1024 * 1024 * 10];
        new Random(22).nextBytes(bytes);
        try {
            Files.write(testFilePath, bytes);

            StatusEnum result = fileDownloader.call();
            Assertions.assertEquals(StatusEnum.Success, result);
            Assertions.assertDoesNotThrow(() -> FilePartUtils.joinAndRemoveFileParts(fileDownloader.getProcessedFiles()));

            String originalChecksum = FilePartUtils.fileChecksum(testFilePath);
            Path downloadedFile = Paths.get(String.valueOf(PrepareDownloadUtils.serverDownloadPath), fileName);
            String downloadedChecksum = FilePartUtils.fileChecksum(downloadedFile);

            Assertions.assertEquals(originalChecksum, downloadedChecksum);
        } finally {
            FilePartUtils.removeFiles(fileDownloader.getProcessedFiles());
            FilePartUtils.removeFile(testFilePath);
        }
    }
}
