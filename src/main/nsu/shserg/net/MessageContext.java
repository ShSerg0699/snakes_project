package nsu.shserg.net;

import nsu.shserg.proto.SnakesProto;
import java.net.InetSocketAddress;

public class MessageContext {
    private boolean isFresh;
    private int port;
    private String address;
    private SnakesProto.GameMessage message;

    public MessageContext(SnakesProto.GameMessage message, InetSocketAddress inetSocketAddress) {
        this.message = message;
        this.isFresh = true;
        this.address = inetSocketAddress.getHostString();
        this.port = inetSocketAddress.getPort();
    }

    public boolean isFresh() {
        return isFresh;
    }

    public void setFresh(boolean fresh) {
        isFresh = fresh;
    }

    public SnakesProto.GameMessage getMessage() {
        return message;
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }
}