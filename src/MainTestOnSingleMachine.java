import client.ClientLogic;
import common.CommonLogic;
import common.exceptions.ConfigurationException;
import common.exceptions.DownloadException;
import common.models.StatusEnum;
import common.models.UdpcastConfiguration;
import server.ServerLogic;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainTestOnSingleMachine {
    public static void main(String[] args) throws DownloadException, InterruptedException, ConfigurationException, ExecutionException {
        UdpcastConfiguration configurationClient = new UdpcastConfiguration(args);
        CommonLogic clientLogic = new ClientLogic(configurationClient);

        //String url = "/home/lubuntu/Desktop/file.file";
        String url = "D:/Studia/Magisterka/ParallelGroupDownloader.zip";
        //String url = "D:\\Programy\\VirtualBoxMachines\\xubuntu-22.04.3-desktop-amd64.iso";
        //String url = "https://cdimage.ubuntu.com/lubuntu/releases/24.04/release/lubuntu-24.04-desktop-amd64.iso";
        String[] serverArgs = new String[]{"-url", url, "-directory", "./", "-filename", "downloadsServer"};
        UdpcastConfiguration configurationServer = new UdpcastConfiguration(serverArgs);
        CommonLogic serverLogic = new ServerLogic(configurationServer);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<StatusEnum> serverResult = executorService.submit(serverLogic::doWork);
        Future<StatusEnum> clientResult = executorService.submit(clientLogic::doWork);

        System.exit(serverResult.get().ordinal() + clientResult.get().ordinal());
    }
}