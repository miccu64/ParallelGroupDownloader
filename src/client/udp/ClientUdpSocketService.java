package client.udp;

import common.DownloadException;
import common.udp.UdpSocketService;
import common.command.Command;
import common.command.CommandType;

import java.beans.PropertyChangeSupport;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientUdpSocketService extends UdpSocketService {
    private final AtomicBoolean downloadStarted = new AtomicBoolean(false);
    private final PropertyChangeSupport propertyChange;

    public ClientUdpSocketService(String multicastIp, int port) throws DownloadException {
        super(multicastIp, port);

        propertyChange = new PropertyChangeSupport(this);
    }

    public boolean getDownloadStarted() {
        return downloadStarted.get();
    }

    private void setDownloadStarted(boolean value) {
        propertyChange.firePropertyChange("downloadStarted", this.downloadStarted.getAndSet(value), value);
    }

    @Override
    protected boolean actionsOnCommandReceive(Command command) {
        boolean result = super.actionsOnCommandReceive(command);

        CommandType type = command.getType();
        switch (type){
            case DownloadStart:
                setDownloadStarted(true);
                break;
            case NextFilePart:

                break;
            case DownloadAbort:
                setDownloadStarted(false);
                // TODO: clear deleted files
                break;
        }
        return result;
    }
}
