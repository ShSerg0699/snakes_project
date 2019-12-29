package nsu.shserg.logic;

import nsu.shserg.proto.SnakesProto;
import nsu.shserg.proto.SnakesProto.GamePlayer;

import java.net.InetAddress;

public class Player {
    private String name;
    private int id;
    private String ipAddress;
    private int port;
    private SnakesProto.NodeRole role ;
    private int score;

    public Player(int id, String name, String ipAddress, int port, SnakesProto.NodeRole role, int score) {
        this.name = name;
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.role = role;
        this.score = score;
    }

    public Player(GamePlayer player) {
        this.name = player.getName();
        this.id = player.getId();
        this.ipAddress = player.getIpAddress();
        this.port = player.getPort();
        this.role = player.getRole();
        this.score = player.getScore();
    }

    public GamePlayer toProto(){
        return GamePlayer.newBuilder()
                .setName(name)
                .setId(id)
                .setIpAddress(ipAddress)
                .setRole(role)
                .setScore(score)
                .setPort(port)
                .build();
    }

    public void setRole(SnakesProto.NodeRole role) {
        this.role = role;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString(){
        return "[" + name + " , " + id + " , " + ipAddress + ":" + port + "]";
    }

    public SnakesProto.NodeRole getRole() {
        return role;
    }

    public void addScore(int newPoints) {
        score += newPoints;
    }
}