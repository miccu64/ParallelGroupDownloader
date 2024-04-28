import common.DownloadException;
import common.udp.FileInfoHolder;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DownloaderTests {
    @Test
    public void testOriginalAndDownloadedFilesEquality() throws IOException, DownloadException {
        PrepareDownloadUtils.initProgram();

        FileInfoHolder fileInfoHolder = new FileInfoHolder();
        SendCommandCallback callback = filePartPath -> {
        };

        String testDirectory = "filesTest";
        Files.createDirectories(Paths.get(testDirectory));
        String fileName = "testOriginalAndDownloadedFilesEquality.file";
        Path testFilePath = Paths.get(testDirectory, fileName);
        URL url = testFilePath.toUri().toURL();
        FileDownloader fileDownloader = new FileDownloader(url.toString(), 1, fileInfoHolder, callback);

        byte[] bytes = new byte[1024 * 1024 * 10];
        new Random(22).nextBytes(bytes);
        try {
            Files.write(testFilePath, bytes);

            int result = fileDownloader.call();
            Assertions.assertEquals(0, result);
            Assertions.assertTrue(FilePartUtils.joinAndDeleteFileParts(new ArrayList<>(fileInfoHolder.filesToProcess)));

            String originalChecksum = FilePartUtils.fileChecksum(testFilePath);
            String downloadedChecksum = FilePartUtils.fileChecksum(PrepareDownloadUtils.serverDownloadPath);

            Assertions.assertEquals(originalChecksum, downloadedChecksum);
        } finally {
            FilePartUtils.removeFileParts(new ArrayList<>(fileInfoHolder.filesToProcess));
            FilePartUtils.removeFileParts(Collections.singletonList(testFilePath));
        }
    }
}
