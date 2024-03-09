package common;

import java.net.InetAddress;

public class Socket {
    private final InetAddress address;
    private final int port;

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public Socket(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public String toString() {
        return address.getHostAddress() + ":" + port;
    }
}
