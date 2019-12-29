package nsu.shserg.net;

import nsu.shserg.SnakesApplication;
import nsu.shserg.characters.Master;
import nsu.shserg.characters.User;
import nsu.shserg.logic.Snake;
import nsu.shserg.proto.SnakesProto.*;
import nsu.shserg.util.ProtoTranslate;
import static nsu.shserg.util.ProtoTranslate.*;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Sender {
    private static final int MAX_MSG_BUF_SIZE = 4096;
    private InetAddress multicastAddress;
    private Master master = null;
    private User user = null;
    private Map<SentMessagesKey, MessageContext> sentMessages;
    private MulticastSocket socket;
    private int multicastPort = 9192;
    private volatile boolean needToSendPing = false;
    private Timer timer;

    public Sender( MulticastSocket socket, Map<SentMessagesKey, MessageContext> sentMessages, InetAddress multicastAddress) {
        this.socket = socket;
        this.sentMessages = sentMessages;
        this.multicastAddress = multicastAddress;
    }

    public Sender(MulticastSocket socket, Map<SentMessagesKey, MessageContext> sentMessages, InetAddress multicastAddress, Master master) {
        this( socket, sentMessages, multicastAddress);
        this.master = master;
    }

    public Sender(MulticastSocket socket, Map<SentMessagesKey, MessageContext> sentMessages, InetAddress multicastAddress, User user) {
        this( socket, sentMessages, multicastAddress);
        this.user = user;
    }

    public void broadcastState(GameMessage message){
        broadcastMessage(message, true);
    }

    public void broadcastMessage(GameMessage message, boolean isConfirmNeed) {
        needToSendPing = false;
        int masterId = 0;
//        if (master.getPlayers().size() == 1 && message.hasPing()) {
//            return;
//        }
        System.out.println("Broadcast: " + message.getTypeCase());

        master.getPlayers().forEach(player -> {
            try {
                if (player.getId() == masterId) {
                    //return;
                }
                System.out.println("addr: " + player.getIpAddress());
                byte[] buf = message.toByteArray();

                InetSocketAddress socketAddress = new InetSocketAddress(player.getIpAddress(), player.getPort());
                socket.send(new DatagramPacket(buf, buf.length, socketAddress));

                if (isConfirmNeed)
                    putIntoSentMessages(message, socketAddress, player.getId());
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("EXCEPTION: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        });
    }

    public void broadcastAnnouncement(GameMessage announcementMsg) {
        needToSendPing = false;
        try {
            byte[] buf = announcementMsg.toByteArray();
            socket.send(new DatagramPacket(buf, buf.length, multicastAddress, multicastPort));
        } catch (IOException e) {
            System.out.println("Error broadcasting announcement");
            e.printStackTrace();
        }
    }

    public boolean sendMessage(InetSocketAddress receiverAddress, GameMessage message, boolean isConfirmNeed) {
        needToSendPing = false;

        System.out.println("Send to " + receiverAddress + " : " + message.getTypeCase());
        try {
            byte[] buf = message.toByteArray();
            if (message.getTypeCase() == GameMessage.TypeCase.JOIN){
                socket.send(new DatagramPacket(buf, buf.length,receiverAddress.getAddress(), 54545));
            }
            socket.send(new DatagramPacket(buf, buf.length, receiverAddress));
        } catch (IOException e){
            System.err.println(receiverAddress);
            return false;
        }

        return true;
    }

    public void setMasterTimer(int pingDelayMs, int nodeTimeoutMs, int stateDelayMs, int masterId){
        timer = new Timer();

        TimerTask masterSendPing  = new TimerTask() {
            @Override
            public void run() {
                if(!needToSendPing){
                    needToSendPing = true;
                    return;
                }
                GameMessage ping =  ProtoTranslate.initPingMessage(masterId);
                broadcastMessage(ping, false);
            }
        };

        TimerTask broadcastAnnouncement  = new TimerTask() {
            @Override
            public void run() {
                if(master.getGameConfig() == null){
                    return;
                }
                GameMessage announcementMsg = ProtoTranslate.getAnnouncementMessage();
                broadcastAnnouncement(announcementMsg);
            }
        };

        TimerTask broadcastState = new TimerTask() {
            @Override
            public void run() {
                master.setGameState();
                SnakesApplication.getGrid().setSnakes(master.getSnakes());
                GameMessage stateMsg = ProtoTranslate.initStateMessage(master.getGameState());
                broadcastState(stateMsg);
            }
        };

        timer.schedule(broadcastState, stateDelayMs, stateDelayMs);
        timer.schedule(masterSendPing, pingDelayMs, pingDelayMs);
        timer.schedule(getCheckAlivePlayersTask(), nodeTimeoutMs, nodeTimeoutMs);
        timer.schedule(broadcastAnnouncement, 1000, 1000);
        timer.schedule(getCheckSentMessagesTask(), nodeTimeoutMs/2, nodeTimeoutMs/2);
    }

    public void stop(){
        if(timer != null)
            timer.cancel();
    }

    public void sendJoin(InetSocketAddress receiverAddress, GameMessage message){
        int masterId = user.getMasterId();

        if(sendMessage(receiverAddress, message, true))
            putIntoSentMessages(message, receiverAddress, masterId);
    }

    public void sendAck(InetSocketAddress receiverAddress, int receiverId, long msgSeq){
        int playerId;
        if(user != null) {
            playerId = user.getPlayerId();
        }
        else {
            playerId = master.getMasterId();
        }
        GameMessage ackMessage = getAckMsg(playerId, receiverId, msgSeq);
        System.out.println("[" + msgSeq + "] ACk: " + receiverAddress);
        sendMessage(receiverAddress, ackMessage, false);
    }

    public void sendConfirmRequiredMessage(InetSocketAddress receiverAddress, GameMessage message, int receiverId){
        if(sendMessage(receiverAddress, message, true))
            putIntoSentMessages(message, receiverAddress, receiverId);
    }

    public void setClientTimer(InetSocketAddress masterAddress, int playerId){
        timer = new Timer();

        TimerTask playerSendPing  = new TimerTask() {
            @Override
            public void run() {
                if(!needToSendPing){
                    needToSendPing = true;
                    return;
                }
                GameMessage ping = ProtoTranslate.initPingMessage(playerId);
                sendMessage(masterAddress, ping, false);
            }
        };

        TimerTask checkMaster = new TimerTask() {
            @Override
            public void run() {
                if(user.isMasterAlive()){
                    user.setMasterAlive(false);
                    return;
                }

                user.changeMaster();
            }
        };

        int nodeTimeoutMs = user.getConfig().getNodeTimeoutMs();
        int pingDelayMs = user.getConfig().getPingDelayMs();

        timer.schedule(playerSendPing, 0, pingDelayMs);
        timer.schedule(checkMaster, nodeTimeoutMs, nodeTimeoutMs);
        timer.schedule(getCheckSentMessagesTask(), nodeTimeoutMs/2, nodeTimeoutMs/2);
    }

    private void putIntoSentMessages(GameMessage message, InetSocketAddress address, int receiverId){
        if(sentMessages.size() >= MAX_MSG_BUF_SIZE){
            return;
        }

        MessageContext context = new MessageContext(message, address);
        SentMessagesKey key = new SentMessagesKey(message.getMsgSeq(), receiverId);
        sentMessages.put(key, context);
    }

    private TimerTask getCheckAlivePlayersTask(){
        int masterId = 0;
        return new TimerTask() {
            @Override
            public void run() {
                var alivePlayers = master.getAlivePlayers();

                for(var iter = alivePlayers.entrySet().iterator(); iter.hasNext(); ) {
                    var entry = iter.next();
                    if(entry.getValue() || entry.getKey() == masterId) {
                        alivePlayers.put(entry.getKey(), false);
                        if(entry.getKey() == 1)
                            System.out.println("1 JHBEKUBHEJLKFJEF");
                        continue;
                    }

                    master.removePlayer(entry.getKey());
                    iter.remove();
                }
            }
        };
    }

    private TimerTask getCheckSentMessagesTask(){
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    for (var iter = sentMessages.entrySet().iterator(); iter.hasNext(); ) {
                        var entry = iter.next();
                        var msgContext = entry.getValue();
                        if (msgContext.isFresh()) {
                            msgContext.setFresh(false);
                            continue;
                        }
                        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(msgContext.getAddress()), msgContext.getPort());
                        sendMessage(address, msgContext.getMessage(), true);

                        iter.remove();
                    }
                } catch(UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}