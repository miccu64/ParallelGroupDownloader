package common.exceptions;

public class InfoFileException extends Exception {
    public InfoFileException(String message) {
        super("InfoFile error: " + message);
    }
}
