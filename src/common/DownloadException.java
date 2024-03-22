package common;

public class DownloadException extends Exception {
    public DownloadException(Exception e, String message) {
        super(message);

        String exceptionName = e.getClass().getCanonicalName();
        message = exceptionName + ": " + message;

        System.err.println(message);
        e.printStackTrace(System.err);
    }
}
