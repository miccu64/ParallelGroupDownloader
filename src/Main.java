import client.ClientLogic;
import common.ILogic;
import common.exceptions.DownloadException;
import common.utils.PrepareDownloadUtils;
import server.ServerLogic;

public class Main {
    public static void main(String[] args) throws DownloadException {
        PrepareDownloadUtils.initProgram();

        //String url = "file:/home/lubuntu/Desktop/file.file";
        String url = "file:/D:/Studia/Magisterka/ParallelGroupDownloader/filesTest/test.file";
        String udpcastPath = "D:\\Studia\\Magisterka\\ParallelGroupDownloader\\udpcast";
        int port = 9000;
        ILogic clientLogic = new ClientLogic(port, udpcastPath);
        ILogic serverLogic = new ServerLogic(url, port, udpcastPath);
        createThread(clientLogic);
        createThread(serverLogic);
    }

    private static Thread createThread(ILogic logic){
        Thread thread = new Thread(logic::doWork);
        thread.start();
        return thread;
    }
}