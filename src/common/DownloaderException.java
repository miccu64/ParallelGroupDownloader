package common;

public class DownloaderException extends Exception {
    public DownloaderException(Exception e, String message) {
        super(message);

        String exceptionName = e.getClass().getCanonicalName();
        message = exceptionName + ": " + message;

        System.err.println(message);
        e.printStackTrace(System.err);
    }
}
