package nsu.shserg.net;

public class SentMessagesKey {
    private long msgSeq;

    private int receiverId;

    public SentMessagesKey(long msgSeq, int receiverId) {
        this.msgSeq = msgSeq;
        this.receiverId = receiverId;
    }

    public long getMsgSeq() {
        return msgSeq;
    }

    public int getPlayerId() {
        return receiverId;
    }
}