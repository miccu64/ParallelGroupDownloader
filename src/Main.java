import common.exceptions.DownloadException;
import common.utils.PrepareDownloadUtils;

public class Main {
    public static void main(String[] args) throws DownloadException {
        PrepareDownloadUtils.initProgram();

        int threadsCount = 2;
        for (int i = 0; i < threadsCount; i++) {
            String url = "file:/home/lubuntu/Desktop/file.file";
        }

        System.out.println("All threads have completed.");
    }
}