package common;

import common.packet.Command;

public interface IUdpService {
    void send(Command command);
}
