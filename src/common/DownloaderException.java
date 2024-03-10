package common;

public class DownloaderException extends Exception {
    public DownloaderException(Exception e, String message) {
        super(message);

        String exceptionName = e.getClass().getCanonicalName();
        message = exceptionName + ": " + message;

        System.out.println(message);
        e.printStackTrace(System.out);
    }
}
