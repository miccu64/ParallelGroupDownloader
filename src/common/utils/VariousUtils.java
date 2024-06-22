package common.utils;

import java.io.OutputStream;
import java.io.PrintStream;

public class VariousUtils {
    public static void sleep(int seconds) {
        try {
            Thread.sleep(1000L * seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void suppressStdErr() {
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        });
        System.setErr(dummyStream);
    }

    public static void suppressStdOut() {
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        });
        System.setOut(dummyStream);
    }
}
