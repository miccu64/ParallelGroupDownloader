package server.udp;

import java.nio.file.Path;

public interface SendCommandCallback {
    void informNewPart(Path filePartPath);
}
