import client.ClientLogic;
import common.ILogic;
import common.exceptions.DownloadException;
import common.utils.PrepareDownloadUtils;
import server.ServerLogic;

public class Main {
    public static void main(String[] args) throws DownloadException {
        PrepareDownloadUtils.initProgram();

        String url = "file:/home/lubuntu/Desktop/file.file";
        String udpcastPath = "D:\\Studia\\Magisterka\\ParallelGroupDownloader";
        int port = 9000;
        ILogic clientLogic = new ClientLogic(port, udpcastPath);
        ILogic serverLogic = new ServerLogic(url, port, udpcastPath);

    }
}