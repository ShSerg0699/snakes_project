package nsu.shserg.util;

import nsu.shserg.logic.Player;
import nsu.shserg.characters.Master;
import nsu.shserg.logic.Point;
import nsu.shserg.logic.Snake;
import nsu.shserg.proto.SnakesProto.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class ProtoTranslate {
        private static final int DEFAULT_MASTER_ID = 0;
        private static final String DEFAULT_MASTER_ADDRESS_STR = "";
        private static AtomicInteger currentGameMsgSeq = new AtomicInteger(0);

        public static GameMessage initPingMessage(int playerId){
            return GameMessage.newBuilder()
                    .setPing(GameMessage.PingMsg.newBuilder().build())
                    .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                    .setSenderId(playerId)
                    .build();
        }

        public static GameState.Coord getCoord(Point point){
            return GameState.Coord.newBuilder().setX(point.getX()).setY(point.getY()).build();
        }


        public static GameState getGameState(Collection<GamePlayer> players, Collection<GameState.Snake> snakes,
                                             GameConfig config, int stateOrder, Collection<GameState.Coord> foods){
            GamePlayers gamePlayers = getGamePlayers(players);
            return GameState.newBuilder()
                    .setPlayers(gamePlayers)
                    .addAllSnakes(snakes)
                    .setConfig(config)
                    .setStateOrder(stateOrder)
                    .addAllFoods(foods)
                    .build();
        }

        public static GameMessage initStateMessage(GameState state){
            GameMessage.StateMsg stateMsg = getStateMsg(state);

            return GameMessage.newBuilder()
                    .setState(stateMsg)
                    .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                    .build();
        }

        public static GameMessage getAnnouncementMessage(){
            GameMessage.AnnouncementMsg announcementMsg = getAnnouncement();

            return GameMessage.newBuilder()
                    .setAnnouncement(announcementMsg)
                    .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                    .build();
        }

        private static GameMessage.AnnouncementMsg getAnnouncement() {
            GameState gameState = Master.getInstance().getGameState();

            return GameMessage.AnnouncementMsg.newBuilder()
                    .setCanJoin(true)
                    .setConfig(gameState.getConfig())
                    .setPlayers(gameState.getPlayers())
                    .build();
        }

        public static GameMessage getAckMsg(int senderId, int receiverId, long msgSeq){
            GameMessage.AckMsg ack =  GameMessage.AckMsg.newBuilder().build();

            return GameMessage.newBuilder()
                    .setAck(ack)
                    .setSenderId(senderId)
                    .setMsgSeq(msgSeq)
                    .setReceiverId(receiverId)
                    .build();
        }


        public static GameMessage getSteerMessage(Direction direction, int playerId){
            GameMessage.SteerMsg steerMsg = GameMessage.SteerMsg.newBuilder()
                    .setDirection(direction)
                    .build();

            return GameMessage.newBuilder()
                    .setSteer(steerMsg)
                    .setSenderId(playerId)
                    .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                    .build();
        }

        public static GameMessage getRoleChangeMessage(NodeRole senderRole, NodeRole receiverRole, int playerId) {
            GameMessage.RoleChangeMsg.Builder roleChangeBuilder = GameMessage.RoleChangeMsg.newBuilder();
            if (senderRole != null)
                roleChangeBuilder.setSenderRole(senderRole);
            if (receiverRole != null)
                roleChangeBuilder.setReceiverRole(receiverRole);

            return GameMessage.newBuilder()
                    .setRoleChange(roleChangeBuilder.build())
                    .setSenderId(playerId)
                    .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                    .build();
        }

        public static GameMessage getJoinMessage(String name, boolean onlyView) {
            GameMessage.JoinMsg joinMsg = GameMessage.JoinMsg.newBuilder()
                    .setName(name)
                    .setOnlyView(onlyView)
                    .build();

            return GameMessage.newBuilder()
                    .setMsgSeq(currentGameMsgSeq.getAndAdd(1))
                    .setJoin(joinMsg)
                    .build();
        }


        private static GameMessage.StateMsg getStateMsg(GameState state){
            return GameMessage.StateMsg.newBuilder()
                    .setState(state)
                    .build();
        }

        private static GamePlayers getGamePlayers(Collection<GamePlayer> players){
            return GamePlayers.newBuilder()
                    .addAllPlayers(players)
                    .build();
        }
    }
