import common.UdpService;
import common.packet.Command;
import common.packet.CommandFindOthers;

import java.io.IOException;

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
        int port = parsePort(args);

        if (args.length > 0){
            UdpService udpService = new UdpService(port);
            udpService.start();
            Thread.sleep(1000);
            Command packet = new CommandFindOthers();
            udpService.sendBroadcast(packet);
            Thread.sleep(100000);
        } else {
            UdpService udpService = new UdpService(port);
            udpService.start();
        }
    }

    private static int parsePort(String[] args){
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

        return port;
    }
}