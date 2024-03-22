import common.DownloadException;
import common.utils.PrepareDownloadUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.FileDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class DownloaderTests {
    @Test
    public void testOriginalAndDownloadedFilesChecksums() throws IOException, NoSuchAlgorithmException, DownloadException {
        String name = "testOriginalAndDownloadedFilesChecksums";
        String destinationDirectoryName = String.valueOf(PrepareDownloadUtils.downloadPath);
        Path testFilePath = Paths.get(destinationDirectoryName, name);
        URL url = testFilePath.toUri().toURL();
        FileDownloader fileDownloader = new FileDownloader(url.toString(), 1);

        byte[] bytes = new byte[1024 * 1024 * 10];
        new Random(22).nextBytes(bytes);
        try {
            Files.write(testFilePath, bytes);

            fileDownloader.downloadWholeFile();
            Assertions.assertTrue(fileDownloader.joinDeleteFileParts());

            byte[] originalChecksum = fileChecksum(testFilePath);
            byte[] downloadedChecksum = fileChecksum(PrepareDownloadUtils.downloadPath);

            Assertions.assertArrayEquals(originalChecksum, downloadedChecksum);
        } finally {
            Files.deleteIfExists(testFilePath);
            for (int i = 0; i < 11; i++) {
                Path path = Paths.get(destinationDirectoryName, name + ".part" + i);
                Files.deleteIfExists(path);
            }
            Files.deleteIfExists(testFilePath);
        }
    }

    private byte[] fileChecksum(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(filePath);
             DigestInputStream ignored = new DigestInputStream(is, md)) {
            return md.digest();
        }
    }
}
