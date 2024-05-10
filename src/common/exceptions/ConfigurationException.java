package common.exceptions;

public class ConfigurationException extends Exception {
    public ConfigurationException(String message) {
        super(message);

        System.err.println(getMessage());
    }

    public ConfigurationException(String arg1, String arg2) {
        super("Wrong key/value pair: " + arg1 + " " + arg2);

        System.err.println(getMessage());
    }
}
