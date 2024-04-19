package server.udp;

import common.command.Command;

import java.nio.file.Path;

public interface SendCommandCallback {
    void informNewPart(Path filePartPath);
}
