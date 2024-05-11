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
            CommonLogic logic;
            if (configuration.getUrl() == null) {
                logic = new ClientLogic(configuration);
            } else {
                logic = new ServerLogic(configuration);
            }
            result = logic.doWork();
        } catch (DownloadException | ConfigurationException e) {
            result = StatusEnum.Error;
        }

        System.exit(result.ordinal());
    }
}