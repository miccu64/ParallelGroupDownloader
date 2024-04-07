package common.udp;

import common.DownloadException;
import common.command.Command;
import common.command.CommandType;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class UdpSocketService implements Runnable, AutoCloseable {
    protected final FileInfoHolder fileInfoHolder = new FileInfoHolder();
    protected final MulticastSocket socket;

    private final InetAddress group;
    private final byte[] buf = new byte[256];
    private final int port;

    public UdpSocketService(String multicastIp, int port) throws DownloadException {
        this.port = port;
        try {
            this.group = InetAddress.getByName(multicastIp);
            socket = new MulticastSocket(this.port);
            socket.joinGroup(group);
        } catch (IOException e) {
            throw new DownloadException(e, "Could not join to multicast: " + multicastIp + ":" + port);
        }
    }

    public void run() {
        while (fileInfoHolder.isInProgress()) {
            DatagramPacket datagram = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(datagram);

                Command receivedCommand = new Command(datagram);
                System.out.println("Received from " + datagram.getSocketAddress() + ": " + receivedCommand);

                new Thread(() -> {
                    boolean shouldLoop = actionsOnCommandReceive(receivedCommand);
                    if (!shouldLoop) {
                        close();
                    }
                }).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (DownloadException ignored) {
            }
        }
    }

    public void send(Command command) {
        DatagramPacket packet = command.createDatagram(group, port);
        try {
            socket.send(packet);
            System.out.println("Sent: " + command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        socket.close();
    }

    protected boolean actionsOnCommandReceive(Command receivedCommand) {
        CommandType type = receivedCommand.getType();
        if (type == CommandType.FindOthers) {
            Command command = new Command(CommandType.ResponseToFindOthers);
            send(command);
        } else if (type == CommandType.DownloadAbort) {
            fileInfoHolder.setErrorStatus();
        }

        return true;
    }
}
