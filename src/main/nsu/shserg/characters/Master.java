package nsu.shserg.characters;

import nsu.shserg.SnakesApplication;
import nsu.shserg.logic.Grid;
import nsu.shserg.logic.Player;
import nsu.shserg.logic.Point;
import nsu.shserg.logic.Snake;
import nsu.shserg.net.*;
import nsu.shserg.proto.SnakesProto;
import nsu.shserg.proto.SnakesProto.GameConfig;
import nsu.shserg.proto.SnakesProto.GameState;
import nsu.shserg.proto.SnakesProto.NodeRole;
import nsu.shserg.proto.SnakesProto.Direction;
import nsu.shserg.util.ProtoTranslate;

import java.io.IOException;
import java.net.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class Master {
    private final int MASTER_ID = 0;
    private Listener listener;
    private Sender sender;
    private GameConfig gameConfig;
    private GameState gameState;
    private User user = User.getInstance();
    private Map<Integer, Snake> snakes = new ConcurrentHashMap<>();
    private Map<Integer, Player> players = new ConcurrentHashMap<>();
    private Map<Integer, Boolean> alivePlayers = new ConcurrentHashMap<>();
    private DatagramSocket socket;
    private MulticastSocket multicastSocket;
    private Map<SentMessagesKey, MessageContext> sentMessages = new ConcurrentHashMap<>();
    private  int multicastPort = 9192;
    private int port = 54544;
    private int stateOrder = 0;

    Master(){
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

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
        user.setGameConfig(gameConfig);
    }

    public GameState getGameState() {
        return gameState;
    }

    public  void setGameState(){
        this.gameState = ProtoTranslate.getGameState(getProtoPlayers(), getProtoSnakes(), gameConfig, 0, SnakesApplication.getGrid().foodsToCoords());
    }

    private List<SnakesProto.GamePlayer> getProtoPlayers(){
        return players.values().stream().map(Player::toProto).collect(Collectors.toList());
    }

    private List<GameState.Snake> getProtoSnakes(){
        return snakes.values().stream().map(Snake::toProtoSnake).collect(Collectors.toList());
    }
    public int getMasterId(){
        return MASTER_ID;
    }

    public void registerPlayerDirection(int senderId, Direction direction) {
        Snake snake = snakes.get(senderId);
        snake.setHeadDirection(direction);
        this.snakes.put(senderId, snake);
    }
///////////////////////////////////////////////////

    public void setAlive(int senderId) {
        this.alivePlayers.put(senderId, true);
    }

    public void checkDeputy(InetSocketAddress address, int id) {
    }

    public int getAvailablePlayerId() {
        return 0;
    }

    public void removePlayer(int senderId) {
    }
    //////////////////////////////////////////////
    public Collection<Player> getPlayers() {
        return players.values();
    }

    public Map<Integer, Boolean> getAlivePlayers() {
        return alivePlayers;
    }

    public void setPlayerAsViewer(int senderId) {
    }

    public void addPlayer(String name, String hostString, int port, NodeRole role) {
        Player player = new Player(players.size(),name, hostString, port, role, 0);
        players.put(player.getId(), player);
        initNewSnake(player);
    }

    public int initNewSnake(Player player) {
        Grid grid = SnakesApplication.getGrid();
        for (int x = 0; x < grid.getRows(); ) {
            outer:
            for (int y = 0; y < grid.getCols(); ++y) {
                for (int i = 0; i < 5; ++i) {
                    if (!checkLine(grid, x, y + i)) {
                        continue outer;
                    }
                }

                List<Point> points = new ArrayList<>();
                points.add(new Point(x + 2, y + 2));
                points.add(new Point(x + 1, y + 2));
                Grid.Cell[][] cells = grid.getCells();
                cells[x + 2][y + 2] = Grid.Cell.SNAKE;
                cells[x + 2][y + 2] = Grid.Cell.SNAKE;
                Snake snake = new Snake(player.getId(), grid, points);
                snakes.put(player.getId(), snake);
                grid.getSnakes().add(snake);
                return 0;
            }
        }
        return 1;
    }


    private static boolean checkLine(Grid grid, int startX, int y) {
        for (int x = startX; x < startX + 5; ++x) {
            if (grid.getCell(x, y) != Grid.Cell.FREE) {
                return false;
            }
        }

        return true;
    }

    public void start() {
        players.put(user.getPlayerId(),user.getPlayer());
        setGameState();
        sender.stop();
        sender.setMasterTimer(gameConfig.getPingDelayMs(), gameConfig.getNodeTimeoutMs(), gameConfig.getStateDelayMs(), MASTER_ID);
    }
    public void stop(){
        sender.stop();
    }

    public Collection<Snake> getSnakes() {
        return snakes.values();
    }


    private static class SingletonHelper{
        private static final Master master = new Master();
    }

    public static Master getInstance() {
        return Master.SingletonHelper.master;
    }
}
