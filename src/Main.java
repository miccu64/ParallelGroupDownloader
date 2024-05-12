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
                "-help - shows help" +
                newLine +
                "-portbase <number>" +
                "- default ports to use. Two ports are used: portbase and portbase+1. Default is 9000. " +
                "The same portbase MUST be specified for both sender and receiver" +
                newLine +
                "-interface <name>" +
                "- network interface used to send out the data" +
                newLine +
                "-url <value>" +
                "- URL to file which will be downloaded. Can be HTTP URL or file on hard drive. " +
                "Path should be absolute. This options makes app working in server mode and it will send file to other computers" +
                newLine +
                "-delay <minutes>" +
                "- server starting delay in minutes. Applicable only to server (when URL is given)");
    }
}