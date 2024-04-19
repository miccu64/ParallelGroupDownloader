import common.DownloadException;
import common.utils.PrepareDownloadUtils;
import server.FileDownloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainFileDownloader {
    public static void main(String[] args) throws DownloadException, MalformedURLException {
        PrepareDownloadUtils.initProgram();
        PrepareDownloadUtils.beforeDownloadCheck(1111);

        Path testFilePath = Paths.get("_master_thesis", "Praca_magisterska.docx");
        URL url = testFilePath.toUri().toURL();
        //String url = "https://getsamplefiles.com/download/zip/sample-1.zip";
//        FileDownloader fileDownloader = new FileDownloader(url.toString(), 1);
//        System.out.println(fileDownloader.getFileSizeInMB());
    }
}
