package nsu.shserg.net;

import nsu.shserg.characters.User;
import nsu.shserg.characters.Master;
import nsu.shserg.proto.SnakesProto;
import nsu.shserg.proto.SnakesProto.*;
import nsu.shserg.proto.SnakesProto.GameMessage.*;
import nsu.shserg.util.GameExecutorService;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Listener {
    private interface Handler{
        void handle(GameMessage message, InetSocketAddress address) throws IOException;
    }

    private static final int BUF_LENGTH = 65000;
    private Master master;
    private User user;
    private Map<SentMessagesKey, MessageContext> sentMessages;
    private Map<TypeCase, Handler> handlers = new HashMap<>();
    private byte[] receiveBuf = new byte[BUF_LENGTH];
    private Sender sender;
    private DatagramSocket socket;
    private InetAddress multicastAddress;
    private long lastStateSeq = 0;
    private volatile boolean isInterrupted = false;
    private volatile long joinMsgSeq = -1;

    public Listener( Sender sender, Map<SentMessagesKey, MessageContext> sentMessages, DatagramSocket socket) {
        this.sender = sender;
        this.sentMessages = sentMessages;
        this.socket = socket;
        initHandlers();
    }

    public Listener( Sender sender, Map<SentMessagesKey, MessageContext> sentMessages, DatagramSocket socket, User user){
        this(sender, sentMessages, socket);
        this.user = user;
    }

    public Listener( Sender sender, Map<SentMessagesKey, MessageContext> sentMessages, DatagramSocket socket, Master master){
        this(sender, sentMessages, socket);
        this.master = master;
    }

    public void listen(){
        GameExecutorService.getExecutorService().submit(() -> {
            GameMessage message;
            TypeCase type;
            DatagramPacket packetToReceive = new DatagramPacket(receiveBuf, BUF_LENGTH);
            isInterrupted = false;
            try {
                //socket.joinGroup(multicastAddress);
                while (!isInterrupted) {
                    socket.receive(packetToReceive);
                    message = SnakesProto.GameMessage.parseFrom(Arrays.copyOf(receiveBuf, packetToReceive.getLength()));

                    type = message.getTypeCase();
                    System.err.println(type);
                    if(type!=TypeCase.PING)
                        System.out.println("[" + message.getMsgSeq() + "] Received type: " + type);

                    handlers.get(type).handle(message, (InetSocketAddress) packetToReceive.getSocketAddress());
                    packetToReceive.setLength(BUF_LENGTH);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    public void interrupt(){
        isInterrupted = true;
    }

    public void reload(){
        joinMsgSeq = -1;
        lastStateSeq = 0;
    }

    private void handleJoin(GameMessage message, InetSocketAddress address){
        System.out.println("JOIN ADDress: " + address);
        JoinMsg joinMsg = message.getJoin();

        NodeRole role = (joinMsg.getOnlyView()) ? NodeRole.VIEWER : NodeRole.NORMAL;
        master.addPlayer(joinMsg.getName(), address.getHostString(), address.getPort(), role);
        //master.checkDeputy(address, id);

        sender.sendAck(address, master.getAvailablePlayerId() - 1, message.getMsgSeq());
    }

    private void handleState(GameMessage message, InetSocketAddress address){
        user.setPlayerAlive(message.getSenderId());
        if(message.getMsgSeq() < lastStateSeq){
            sender.sendAck(address, user.getMasterId(), message.getMsgSeq());
            return;
        }

        lastStateSeq = message.getMsgSeq();
        user.setGameState(message.getState().getState());
        user.updateAllPoints();

        sender.sendAck(address, user.getMasterId(), message.getMsgSeq());
    }

    private void handleAck(GameMessage message, InetSocketAddress address){
        if(joinMsgSeq == message.getMsgSeq()) {
            System.out.println("GET JOIN ACK");
            sender.setClientTimer(address, message.getReceiverId());
            user.setPlayerId(message.getReceiverId());
        }

        sentMessages.entrySet().removeIf(e -> e.getKey().getMsgSeq() == message.getMsgSeq() &&
                e.getKey().getPlayerId() == message.getSenderId());
    }

    private void handlePing(GameMessage message, InetSocketAddress address){
        System.out.println("PING : " + message.getSenderId());
        user.setPlayerAlive(message.getSenderId());
    }

    private void handleSteer(GameMessage message, InetSocketAddress address){
        //user.setPlayerAlive(message.getSenderId());
        Direction direction = message.getSteer().getDirection();
        master.registerPlayerDirection(message.getSenderId(), direction);

        sender.sendAck(address, 0, message.getMsgSeq());
    }

    private void handleError(GameMessage message, InetSocketAddress address){
        String errorMessage = message.getError().getErrorMessage();
        user.error(errorMessage);

        sender.sendAck(address, user.getMasterId(), message.getMsgSeq());
    }

    private void handleRoleChange(GameMessage message, InetSocketAddress address){
        user.setPlayerAlive(message.getSenderId());
        RoleChangeMsg roleChangeMsg = message.getRoleChange();
        if(roleChangeMsg.getSenderRole() == NodeRole.MASTER){
            user.setMasterAddress(address);
        }
        else if(roleChangeMsg.getSenderRole() == NodeRole.VIEWER){
            master.setPlayerAsViewer(message.getSenderId());
        }

        if(roleChangeMsg.getReceiverRole() == NodeRole.DEPUTY){
            user.setRole(NodeRole.DEPUTY);
        }
        else if(roleChangeMsg.getReceiverRole() == NodeRole.MASTER){
            user.becomeMaster();
        }

        sender.sendAck(address, user.getMasterId(), message.getMsgSeq());
    }

    private void handleAnnouncement(GameMessage message, InetSocketAddress address){
    }

    private void initHandlers(){
        handlers.put(TypeCase.ACK, this::handleAck);
        handlers.put(TypeCase.JOIN, this::handleJoin);
        handlers.put(TypeCase.PING, this::handlePing);
        handlers.put(TypeCase.STATE, this::handleState);
        handlers.put(TypeCase.ERROR, this::handleError);
        handlers.put(TypeCase.STEER, this::handleSteer);
        handlers.put(TypeCase.ANNOUNCEMENT, this::handleAnnouncement);
        handlers.put(TypeCase.ROLE_CHANGE, this::handleRoleChange);
    }
}