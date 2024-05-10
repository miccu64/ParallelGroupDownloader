package common;

import common.exceptions.ConfigurationException;

public class UdpcastConfiguration {
    public int portbase = 9000;
    public int startMaxWaitInSeconds = 6;
    public String url;
    public String networkInterface;

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
                        if (portbase < 1) {
                            throw new ConfigurationException("Portbase must be > 0.");
                        }
                        break;
                    case "start-max-wait":
                        startMaxWaitInSeconds = Integer.parseUnsignedInt(value);
                        if (startMaxWaitInSeconds < 6) {
                            throw new ConfigurationException("Start max wait must be >= 6.");
                        }
                        break;
                    case "url":
                        url = value;
                        break;
                    case "interface":
                        networkInterface = value;
                        break;
                    default:
                        throw new ConfigurationException(key, value);
                }
            } catch (RuntimeException e) {
                throw new ConfigurationException(key, value);
            }
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
