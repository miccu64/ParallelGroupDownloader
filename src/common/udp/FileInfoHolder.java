package common.udp;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileInfoHolder {

    public final ConcurrentLinkedQueue<Path> processedFiles = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<Path> filesToProcess = new ConcurrentLinkedQueue<>();
}
