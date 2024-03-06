import common.BroadcastService;
import common.UdpService;
import server.FileDownloader;

import java.io.IOException;
import java.util.Arrays;

public class Main {
//    public static void main(String[] args) throws IOException {
//        String url = "https://getsamplefiles.com/download/zip/sample-1.zip";
//        int blockSizeInMB = 1;
//        String fileName = "test.zip";
//
//        FileDownloader downloader = new FileDownloader(url, blockSizeInMB, fileName);
//
//        if (downloader.downloadWholeFile())
//            downloader.joinDeleteFileParts();
//    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 5000;
        if (args.length > 0) {
            try {
                int parsedPort = Integer.parseInt(args[0]);
                int startRange = 1024;
                int endRange = 65535;
                if (parsedPort < startRange || parsedPort > endRange) {
                    System.out.printf("Port number must be number between <%s, %s>.%n", startRange, endRange);
                } else {
                    port = parsedPort;
                }
            } catch (Exception ignored) {
                System.out.println("Could not parse given port number.");
            }
        }

        System.out.println("Using port: " + port);

        if (args.length > 0){
            Thread.sleep(5000);
            BroadcastService broadcastService = new BroadcastService(port);
            broadcastService.sendBroadcast("aaa");
            Thread.sleep(100000);
        } else {
            UdpService udpService = new UdpService(port);
            udpService.start();
        }
    }
}