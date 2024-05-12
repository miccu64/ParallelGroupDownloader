import client.ClientLogic;
import common.CommonLogic;
import common.exceptions.ConfigurationException;
import common.exceptions.DownloadException;
import common.models.UdpcastConfiguration;
import server.ServerLogic;

public class MainTestOnSingleMachine {
    public static void main(String[] args) throws DownloadException, InterruptedException, ConfigurationException {
        //String url = "/home/lubuntu/Desktop/file.file";
        //String url = "file:/D:/Studia/Magisterka/ParallelGroupDownloader.zip";
        //String url = "D:\\Programy\\VirtualBoxMachines\\xubuntu-22.04.3-desktop-amd64.iso";
        String url = "https://cdimage.ubuntu.com/lubuntu/releases/24.04/release/lubuntu-24.04-desktop-amd64.iso";
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

    private static Thread createThread(CommonLogic logic) {
        Thread thread = new Thread(logic::doWork);
        thread.start();
        return thread;
    }
}