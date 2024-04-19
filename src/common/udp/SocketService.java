package common.udp;

import common.DownloadException;
import common.command.Command;
import common.command.CommandType;

import java.io.IOException;
import java.net.*;

public abstract class SocketService implements Runnable, AutoCloseable {
    protected final FileInfoHolder fileInfoHolder;
    protected final MulticastSocket socket;

    private final InetAddress group;
    private final byte[] buf = new byte[256];
    private final int port;
    private final Thread udpcastThread;

    public SocketService(String multicastIp, int port, FileInfoHolder fileInfoHolder, UdpcastService udpcastService) throws DownloadException {
        this.port = port;
        this.fileInfoHolder = fileInfoHolder;

        try {
            this.group = InetAddress.getByName(multicastIp);
            socket = new MulticastSocket(this.port);
            socket.joinGroup(group);
        } catch (IOException e) {
            throw new DownloadException(e, "Could not join to multicast: " + multicastIp + ":" + port);
        }

        udpcastThread = new Thread(() -> {
            int result = udpcastService.call();
            close();
            System.exit(result);
        });
    }

    public void run() {
        while (fileInfoHolder.isInProgress()) {
            DatagramPacket datagram = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(datagram);

                Command receivedCommand = new Command(datagram);
                System.out.println("Received from " + datagram.getSocketAddress() + ": " + receivedCommand);

                boolean shouldLoop = actionsOnCommandReceive(receivedCommand);
                if (!shouldLoop) {
                    close();
                }
            } catch (SocketTimeoutException | SocketException | DownloadException ignored) {
            } catch (IOException e) {
                throw new RuntimeException(e);
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
        udpcastThread.interrupt();
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

    protected void startUdpcastThread() {
        udpcastThread.start();
    }
}
