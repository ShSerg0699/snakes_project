package nsu.shserg.net;

import java.net.InetSocketAddress;

public class AnnouncementContext {
    private boolean isActual;

    private InetSocketAddress masterAddress;

    public AnnouncementContext(InetSocketAddress masterAddress) {
        this.isActual = true;
        this.masterAddress = masterAddress;
    }

    public boolean isActual() {
        return isActual;
    }

    public void setActual(boolean actual) {
        isActual = actual;
    }

    public InetSocketAddress getMasterAddress() {
        return masterAddress;
    }

    public void setMasterAddress(InetSocketAddress masterAddress) {
        this.masterAddress = masterAddress;
    }
}