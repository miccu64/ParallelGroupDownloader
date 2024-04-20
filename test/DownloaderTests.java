import common.DownloadException;
import common.udp.FileInfoHolder;
import common.utils.FilePartUtils;
import common.utils.PrepareDownloadUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.FileDownloader;
import server.udp.SendCommandCallback;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DownloaderTests {
    @Test
    public void testOriginalAndDownloadedFilesChecksums() throws IOException, NoSuchAlgorithmException, DownloadException {
        PrepareDownloadUtils.initProgram();

        FileInfoHolder fileInfoHolder = new FileInfoHolder();
        SendCommandCallback callback = filePartPath -> {
        };

        String testDirectory = "filesTest";
        Files.createDirectories(Paths.get(testDirectory));
        String fileName= "testOriginalAndDownloadedFilesChecksums.file";
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

            byte[] originalChecksum = fileChecksum(testFilePath);
            byte[] downloadedChecksum = fileChecksum(PrepareDownloadUtils.serverDownloadPath);

            Assertions.assertArrayEquals(originalChecksum, downloadedChecksum);
        } finally {
            FilePartUtils.removeFileParts(new ArrayList<>(fileInfoHolder.filesToProcess));
            FilePartUtils.removeFileParts(Collections.singletonList(testFilePath));
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
