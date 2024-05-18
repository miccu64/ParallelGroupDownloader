import client.ClientLogic;
import common.CommonLogic;
import common.exceptions.ConfigurationException;
import common.exceptions.DownloadException;
import common.models.StatusEnum;
import common.models.UdpcastConfiguration;
import server.ServerLogic;

public class Main {
    public static void main(String[] args) {
        StatusEnum result;
        try {
            UdpcastConfiguration configuration = new UdpcastConfiguration(args);
            if (configuration.isHelpInvoked()) {
                showHelp();
                return;
            }

            CommonLogic logic;
            if (configuration.getUrl() == null) {
                logic = new ClientLogic(configuration);
            } else {
                logic = new ServerLogic(configuration);
            }
            result = logic.doWork();
        } catch (ConfigurationException e) {
            showHelp();
            result = StatusEnum.Error;
        } catch (DownloadException e) {
            result = StatusEnum.Error;
        }

        System.exit(result.ordinal());
    }

    private static void showHelp() {
        String newLine = "\n";
        System.out.println("Available options (each one is optional):" +
                newLine +
                "Common options (for both client and server):" +
                newLine +
                "-help - shows help, application will exit after help display" +
                newLine +
                "-portbase <number>" +
                "- two ports are used: portbase and portbase+1. Default is 9000. " +
                "The same portbase MUST be specified for both sender and receiver. Ports from range <1024-65534> are allowed" +
                newLine +
                "-interface <name>" +
                "- network interface used to send out the data" +
                newLine +
                "-directory <value> " +
                "- directory to which will be saved downloaded files" +
                newLine +
                "-filename <value> " +
                "- destination file name (if name not given it will be taken from URL from server)"
        );
        System.out.println("Server only options:" +
                newLine +
                "-url <value>" +
                "- URL to file which will be downloaded. Can be HTTP URL or file on hard drive. " +
                "Path should be absolute. This options makes app working in server mode and it will send file to other computers" +
                newLine +
                "-delay <minutes>" +
                "- sending file to clients delay in minutes" +
                newLine +
                "-blocksize <MegaBytes> " +
                "- size of single file part in MB to which original file will be split"
        );
        System.out.println("Aim of this project is to quickly download and send big files between many PCs." +
                "It uses udpcast packet, which is bundled inside this app. It works on Windows and most of Linux distros." +
                "Computers must be in the same local network."
        );
    }
}