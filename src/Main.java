import client.ClientLogic;
import common.CommonLogic;
import common.exceptions.DownloadException;
import server.ServerLogic;

public class Main {
    public static void main(String[] args) throws DownloadException, InterruptedException {
        //String url = "file:/home/lubuntu/Desktop/file.file";
        String url = "file:/D:/Studia/Magisterka/ParallelGroupDownloader.zip";
        String udpcastPath = "D:\\Studia\\Magisterka\\ParallelGroupDownloader\\udpcast";
        int port = 9000;
        CommonLogic clientLogic = new ClientLogic(port, udpcastPath);
        CommonLogic serverLogic = new ServerLogic(url, port, udpcastPath);
        Thread client = createThread(clientLogic);
        Thread server = createThread(serverLogic);
        client.join();
        server.join();
        System.exit(0);
    }

    private static void futureMain(String[] args) {
        
    }

    private static Thread createThread(CommonLogic logic) {
        Thread thread = new Thread(logic::doWork);
        thread.start();
        return thread;
    }
}