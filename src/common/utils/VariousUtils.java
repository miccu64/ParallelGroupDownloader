package common.utils;

public class VariousUtils {
    public static void sleep(int seconds) {
        try {
            Thread.sleep(1000L * seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
