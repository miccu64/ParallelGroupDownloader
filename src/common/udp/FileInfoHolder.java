package common.udp;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FileInfoHolder {
    public final ConcurrentLinkedQueue<Path> processedFiles = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<Path> filesToProcess = new ConcurrentLinkedQueue<>();

    public final AtomicBoolean canBecomeServer = new AtomicBoolean(true);
    public final AtomicInteger expectedPartsCount = new AtomicInteger(-1);

    public int getFilesCount(){
        HashSet<Path> paths = new HashSet<>();
        paths.addAll(filesToProcess);
        paths.addAll(processedFiles);
        return paths.size();
    }

    private final int inProgress = -1;
    private final int success = 0;
    private final int error = 1;
    private final AtomicInteger status = new AtomicInteger(inProgress);

    public void setErrorStatus() {
        status.set(error);
    }

    public void setSuccessStatus() {
        status.set(success);
    }

    public boolean isInProgress() {
        return status.get() == inProgress;
    }

    public boolean isError() {
        return status.get() == error;
    }

    public boolean isSuccess() {
        return status.get() == success;
    }
}
