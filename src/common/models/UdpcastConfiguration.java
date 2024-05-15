package common.models;

import common.exceptions.ConfigurationException;

import java.util.Arrays;

public class UdpcastConfiguration {
    private boolean isHelpInvoked = false;
    private int portbase = 9000;
    private int delayMinutes = 0;
    private String url;
    private String networkInterface;
    private String directory;

    public boolean isHelpInvoked() {
        return isHelpInvoked;
    }

    public int getPortbase() {
        return portbase;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public String getUrl() {
        return url;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public String getDirectory() {
        return directory;
    }

    public UdpcastConfiguration(String[] args) throws ConfigurationException {
        if (Arrays.asList(args).contains("-help")) {
            isHelpInvoked = true;
            return;
        }

        if (args.length % 2 != 0) {
            throw new ConfigurationException("One of given parameters does not have corresponding value.");
        }

        for (int i = 0; i < args.length; i += 2) {
            String key = getKey(args[i]);
            String value = getValue(args[i + 1]);
            if (value.isEmpty()){
                throw new ConfigurationException("Empty value was given.");
            }

            try {
                switch (key) {
                    case "portbase":
                        portbase = Integer.parseUnsignedInt(value);
                        if (portbase < 1024 || portbase > 65535) {
                            throw new ConfigurationException("Only ports 1024-65535 are allowed.");
                        }
                        break;
                    case "interface":
                        networkInterface = value;
                        break;
                    case "delay":
                        delayMinutes = Integer.parseUnsignedInt(value);
                        if (delayMinutes >= 30) {
                            throw new ConfigurationException("Delay must be shorter than 30 minutes.");
                        }
                        break;
                    case "url":
                        url = value;
                        break;
                    case "directory":
                        directory = value;
                        break;
                    default:
                        throw new ConfigurationException(key, value);
                }
            } catch (RuntimeException e) {
                throw new ConfigurationException(key, value);
            }
        }

        if (url == null && delayMinutes > 0) {
            throw new ConfigurationException("Delay option is applicable only when URL is given.");
        }
    }

    private String getKey(String key) throws ConfigurationException {
        if (!key.startsWith("-") || key.length() == 1) {
            throw new ConfigurationException("Wrong key: " + key);
        }
        return key.substring(1).toLowerCase();
    }

    private String getValue(String value) throws ConfigurationException {
        if (value.isEmpty()) {
            throw new ConfigurationException("Empty value was given.");
        }
        return value;
    }
}
