package common;

import common.exceptions.ConfigurationException;

public class UdpcastConfiguration {
    private int portbase = 9000;
    private int delayMinutes = 0;
    private String url;
    private String networkInterface;

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

    public UdpcastConfiguration(String[] args) throws ConfigurationException {
        if (args.length % 2 != 0) {
            throw new ConfigurationException("One of given parameters does not have corresponding value.");
        }

        for (int i = 0; i < args.length; i += 2) {
            String key = getKey(args[i]);
            String value = getValue(args[i + 1]);

            try {
                switch (key) {
                    case "portbase":
                        portbase = Integer.parseUnsignedInt(value);
                        if (getPortbase() < 1) {
                            throw new ConfigurationException("Portbase must be > 0.");
                        }
                        break;
                    case "interface":
                        networkInterface = value;
                        break;
                    case "delay":
                        delayMinutes = Integer.parseUnsignedInt(value);
                        if (getDelayMinutes() >= 30) {
                            throw new ConfigurationException("Delay must be shorter than 30 minutes.");
                        }
                        break;
                    case "url":
                        url = value;
                        break;
                    default:
                        throw new ConfigurationException(key, value);
                }
            } catch (RuntimeException e) {
                throw new ConfigurationException(key, value);
            }
        }

        if (getUrl() == null && getDelayMinutes() > 0) {
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
