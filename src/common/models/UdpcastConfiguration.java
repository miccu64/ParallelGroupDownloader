package common.models;

import common.exceptions.ConfigurationException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class UdpcastConfiguration {
    private boolean isHelpInvoked = false;
    private int portbase = 9000;
    private String networkInterface;
    private String directory = "downloads";
    private String fileName;

    private String url;
    private int delayMinutes = 0;
    private int blockSizeInMb = 64;

    public boolean isHelpInvoked() {
        return isHelpInvoked;
    }

    public int getPortbase() {
        return portbase;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public String getDirectory() {
        return directory;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public int getBlockSizeInMb() {
        return blockSizeInMb;
    }

    public UdpcastConfiguration(String[] args) throws ConfigurationException {
        if (Arrays.asList(args).contains("-help")) {
            isHelpInvoked = true;
            return;
        }

        if (args.length % 2 != 0) {
            throw new ConfigurationException("One of given parameters does not have corresponding value.");
        }

        boolean serverOptionGiven = false;
        for (int i = 0; i < args.length; i += 2) {
            String key = getKey(args[i]);
            String value = getValue(args[i + 1]);
            if (value.isEmpty()) {
                throw new ConfigurationException("Empty value was given.");
            }

            try {
                switch (key) {
                    case "portbase":
                        portbase = Integer.parseInt(value);
                        if (portbase < 1024 || portbase > 65535) {
                            throw new ConfigurationException("Only ports 1024-65535 are allowed.");
                        }
                        break;
                    case "interface":
                        networkInterface = value;
                        break;
                    case "directory":
                        this.directory = String.valueOf(parsePath(value));
                        break;
                    case "filename":
                        Path path = parsePath(value);
                        if (path.getParent() != null || path.getFileName() == null) {
                            throw new ConfigurationException("Wrong file name.");
                        }
                        this.fileName = String.valueOf(path);
                        break;

                    case "url":
                        url = value;
                        break;
                    case "delay":
                        serverOptionGiven = true;
                        delayMinutes = Integer.parseUnsignedInt(value);
                        if (delayMinutes >= 30) {
                            throw new ConfigurationException("Delay must be shorter than 30 minutes.");
                        }
                        break;
                    case "blocksize":
                        serverOptionGiven = true;
                        blockSizeInMb = Integer.parseInt(value);
                        if (blockSizeInMb <= 0) {
                            throw new ConfigurationException("Block size must be at least equal 1MB.");
                        }
                        break;
                    default:
                        throw new ConfigurationException(key, value);
                }
            } catch (RuntimeException e) {
                throw new ConfigurationException(key, value);
            }
        }

        if (url == null && serverOptionGiven) {
            throw new ConfigurationException("Delay and blocksize options are applicable only when URL is given (when acting as a server).");
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

    private Path parsePath(String value) throws ConfigurationException {
        try {
            return Paths.get(value);
        } catch (InvalidPathException e){
            throw new ConfigurationException("Invalid path of filename or directory.");
        }
    }
}
