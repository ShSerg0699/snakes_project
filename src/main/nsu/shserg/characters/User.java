package nsu.shserg.characters;

import nsu.shserg.logic.*;
import nsu.shserg.net.Listener;
import nsu.shserg.net.MessageContext;
import nsu.shserg.net.Sender;
import nsu.shserg.net.SentMessagesKey;
import nsu.shserg.proto.SnakesProto;
import nsu.shserg.util.ProtoTranslate;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private Sender sender;
    private Listener listener;
    private Snake snake;
    private Player player;
    private Grid grid;
    private SnakesProto.GameConfig gameConfig;
    private SnakesProto.GameState gameState;
    private DatagramSocket socket;
    private MulticastSocket multicastSocket;
    private InetSocketAddress masterAddress;
    private Map<SentMessagesKey, MessageContext> sentMessages = new ConcurrentHashMap<>();
    private int port = 45455;
    private int multicastPort = 9192;

    User(){
        player = new Player(-1,"", "", port,SnakesProto.NodeRole.NORMAL, 0);
        try {
            socket = new DatagramSocket(port);
            multicastSocket = new MulticastSocket(multicastPort);
            sender = new Sender(multicastSocket, sentMessages, InetAddress.getByName("239.192.0.4"), this);
            listener = new Listener(sender, sentMessages, socket, this);
            listener.listen();
        } catch (IOException e){
            System.err.println(e.getLocalizedMessage());
        }
    }

//    public int initSnake() {
//        for (int x = 0; x < grid.getRows(); ) {
//            outer:
//            for (int y = 0; y < grid.getCols(); ++y) {
//                for (int i = 0; i < 5; ++i) {
//                    if (!checkLine(grid, x, y + i)) {
//                        continue outer;
//                    }
//                }
//
//                List<Point> points = new ArrayList<>();
//                points.add(new Point(x + 2, y + 2));
//                points.add(new Point(x + 1, y + 2));
//                Grid.Cell[][] cells = grid.getCells();
//                cells[x + 2][y + 2] = Grid.Cell.SNAKE;
//                cells[x + 2][y + 2] = Grid.Cell.SNAKE;
//                snake = new Snake(player.getId(), grid, points);
//                grid.getSnakes().add(snake);
//                return 0;
//            }
//        }
//        return 1;
//    }
//
//    private static boolean checkLine(Grid grid, int startX, int y) {
//        for (int x = startX; x < startX + 5; ++x) {
//            if (grid.getCell(x, y) != Grid.Cell.FREE) {
//                return false;
//            }
//        }
//
//        return true;
//    }

    public void setGrid(Grid grid){
        this.grid = grid;
    }

    public Grid getGrid(){
        return grid;
    }

    public void sendDirection(SnakesProto.Direction direction) {
        //snake.setHeadDirection(direction);
        SnakesProto.GameMessage steerMsg = ProtoTranslate.getSteerMessage(direction, getPlayerId());
        sender.sendConfirmRequiredMessage(masterAddress, steerMsg, getPlayerId());
    }

    public Player getPlayer(){
        return player;
    }

    public void setPlayerName(String text) {
        this.player.setName(text);
    }

    public String getPlayerName() {
        return player.getName();
    }

    public void setPlayerId(int receiverId) {
        this.player.setId(receiverId);
    }

    public int getPlayerId() {
        return player.getId();
    }

    public SnakesProto.GameConfig getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(SnakesProto.GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    public void sendJoinMsg(InetSocketAddress receiver, String name){
        SnakesProto.GameMessage gameMsg = ProtoTranslate.getJoinMessage(name, false);
        sender.sendJoin(receiver, gameMsg);
    }

    public SnakesProto.GameState getGameState() {
        return gameState;
    }

    public void setGameState(SnakesProto.GameState gameState){
        this.gameState = gameState;
    }

    public void setMasterAddress(InetSocketAddress address) {
        this.masterAddress = address;
    }

    public SnakesProto.GameConfigOrBuilder getConfig() {
        return gameConfig;
    }

    //////////////////////////////

    public void updateAllPoints() {
        int i= 0;
        for(Snake snake : grid.getSnakes()){
            snake.setHeadDirection(gameState.getSnakes(i).getHeadDirection());
            i++;
        }
        List<Food> foods =new Vector<>();
        for(int j = 0; j < gameState.getFoodsCount(); j++){
            foods.add(new Food(new Point(gameState.getFoods(j).getX(), gameState.getFoods(j).getY())));
        }
        grid.setFoods(foods);

    }

    public int getMasterId() {
        return 0;
    }

    public void setPlayerAlive(int senderId) {
    }

    public void error(String errorMessage) {
    }

    public void setRole(SnakesProto.NodeRole deputy) {
    }

    public void becomeMaster() {
    }

//    public void addAvailableGame(SnakesProto.GameMessage.AnnouncementMsg announcement, InetSocketAddress address) {
//    }

    public boolean isMasterAlive() {
        return true;
    }

    public void setMasterAlive(boolean b) {
    }

    public void changeMaster() {
    }
    ////////////////////////////////////////
    private static class SingletonHelper{

        private static final User user = new User();
    }
    
    public static User getInstance() {
        return User.SingletonHelper.user;
    }
}
