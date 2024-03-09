import common.Socket;
import common.UdpService;
import common.packet.Command;
import common.packet.CommandFindOthers;
import common.packet.CommandHelpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
        for (int destinationPort : otherInstancesPorts){
            Socket destinationSocket = new Socket(CommandHelpers.getBroadcastAddress(), destinationPort);
            Command packet = new CommandFindOthers(listenPort, destinationSocket);
            udpService.send(packet);
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter download URL: ");
        String url = scanner.nextLine();
        System.out.println(url);
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