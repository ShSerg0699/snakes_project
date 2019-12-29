package nsu.shserg.logic;

import javafx.scene.paint.Color;
import nsu.shserg.proto.SnakesProto;
import nsu.shserg.proto.SnakesProto.Direction;
import nsu.shserg.proto.SnakesProto.GameState.Snake.*;
import nsu.shserg.proto.SnakesProto.GameState.Coord;

import java.util.List;
import java.util.Vector;

public class Snake {
    public static final Color COLOR = Color.GREEN;
    public static final Color DEAD = Color.RED;
    private SnakeState snakeState = SnakeState.ALIVE;
    private int playerId;
    private Grid grid;
    private boolean safe;
    private List<Point> points;
    private Point head;
    private Direction headDirection = Direction.RIGHT;
    private int xVelocity;
    private int yVelocity;

    public Snake(int playerId, Grid grid, List<Point> points) {
        this.playerId = playerId;
        this.points = points;
        head = points.get(0);
        safe = true;
        this.grid = grid;
        xVelocity = 0;
        yVelocity = 0;
    }

    private void shiftTo(Point point) {
        Grid.Cell[][] cells = grid.getCells();
        cells[points.get(0).getX()][points.get(0).getY()] = Grid.Cell.FREE;
        points.remove(0);
        checkAndAdd(point);
    }

    private void checkAndAdd(Point point) {
        point = grid.wrap(point);
        for(Snake snake : grid.getSnakes()) {
            safe &= !snake.getPoints().contains(point);
        }
        points.add(point);
        Grid.Cell[][] cells = grid.getCells();
        cells[point.getX()][point.getY()] = Grid.Cell.SNAKE;
        head = point;
    }

    public List<Point> getPoints() {
        return points;
    }

    public boolean isSafe() {
        return safe;
    }

    public Point getHead() {
        return head;
    }

    private boolean isStill() {
        return xVelocity == 0 & yVelocity == 0;
    }

    public void move() {
        if (!isStill()) {
            shiftTo(head.translate(xVelocity, yVelocity));
        }
    }

    public void extend() {
        if (!isStill()) {
            checkAndAdd(head.translate(xVelocity, yVelocity));
        }
        for(Food food : grid.getFoods()){
            if (food.getPoint().equals(getHead())) {
                extend();
                food.setPoint(grid.getRandomPoint());
            }
        }
    }

    private List<Coord> pointsToCoords(){
        List<Coord> coords = new Vector<>();
        for (Point point : points) {
            coords.add(Coord.newBuilder().setX(point.getX()).setY(point.getX()).build());
        }
        return coords;
    }

    public SnakesProto.GameState.Snake toProtoSnake(){
        return SnakesProto.GameState.Snake.newBuilder()
                .setState(snakeState)
                .setHeadDirection(headDirection)
                .addAllPoints(pointsToCoords())
                .setPlayerId(playerId)
                .build();
    }

    public SnakesProto.Direction getHeadDirection() {
        return headDirection;
    }

    public void setHeadDirection(SnakesProto.Direction headDirection) {
        if ((this.headDirection == Direction.LEFT && headDirection == Direction.RIGHT) ||
            (this.headDirection == Direction.RIGHT && headDirection == Direction.LEFT) ||
            (this.headDirection == Direction.UP && headDirection == Direction.DOWN) ||
            (this.headDirection == Direction.DOWN && headDirection == Direction.UP)){
            return;
        }
        this.headDirection = headDirection;
        xVelocity = (headDirection == Direction.LEFT) ? -1 : (headDirection == Direction.RIGHT) ? 1 : 0;
        yVelocity = (headDirection == Direction.UP) ? -1 : (headDirection == Direction.DOWN) ? 1 : 0;
    }
}