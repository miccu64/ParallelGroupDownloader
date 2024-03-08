import common.UdpService;
import common.packet.Command;
import common.packet.CommandFindOthers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<Integer> otherInstancesPorts = new ArrayList<>(Arrays.asList(10100, 10101, 10102));
        int listenPort = parsePort(args);
        otherInstancesPorts.remove(Integer.valueOf(listenPort));

        UdpService udpService = new UdpService(listenPort, otherInstancesPorts);
        udpService.start();
        Thread.sleep(5000);
        Command packet = new CommandFindOthers(String.valueOf(listenPort));
        udpService.sendBroadcast(packet);
        Thread.sleep(100000);
    }

    private static int parsePort(String[] args) {
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