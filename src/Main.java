import client.ClientLogic;
import common.CommonLogic;
import common.UdpcastConfiguration;
import common.exceptions.ConfigurationException;
import common.exceptions.DownloadException;
import server.ServerLogic;

public class Main {
    public static void main(String[] args) throws DownloadException, InterruptedException, ConfigurationException {
        //String url = "file:/home/lubuntu/Desktop/file.file";
        //String url = "file:/D:/Studia/Magisterka/ParallelGroupDownloader.zip";
        String url = "D:\\Programy\\VirtualBoxMachines";
        UdpcastConfiguration configurationClient = new UdpcastConfiguration(args);
        CommonLogic clientLogic = new ClientLogic(configurationClient);
        String[] serverArgs = new String[]{"-url", url};
        UdpcastConfiguration configurationServer = new UdpcastConfiguration(serverArgs);
        CommonLogic serverLogic = new ServerLogic(configurationServer);
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