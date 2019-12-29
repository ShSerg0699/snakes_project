package nsu.shserg.logic;

import nsu.shserg.characters.User;
import nsu.shserg.controllers.GameController;
import nsu.shserg.proto.SnakesProto;
import nsu.shserg.tablels_items.RatingTableItems;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;


import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class Grid {
    public enum Cell{
        SNAKE,
        FOOD,
        HEAD,
        FREE
    }

    public static final Color COLOR = Color.CORNSILK;
    private final int cols;     // The number of columns
    private final int rows;     // The number of rows
    private GameController gameController;
    private Collection<Snake> snakes = new Vector<>();
    private List<Food> foods = new Vector<>();
    private int cellSize;

    private Cell[][] cells;

    public Grid(final double width, final double height, int cellSize, GameController gameController) {
        this.cellSize = cellSize;
        this.gameController = gameController;
        rows = (int) width / cellSize;
        cols = (int) height / cellSize;
        this.cells = new Cell[(int)width][(int)height];
        for(int i=0; i < rows; i++){
            for(int j=0;j<cols; j++){
                cells[i][j] = Cell.FREE;
            }
        }


        // put the food at a random location
        for(int i = 0; i < User.getInstance().getGameConfig().getFoodStatic() + User.getInstance().getConfig().getFoodPerPlayer() * snakes.size(); i++) {
            Point point = getRandomPoint();
            foods.add(new Food(point));
            cells[point.getX()][point.getY()] = Cell.FOOD;
        }
    }

    public Point wrap(Point point) {
        int x = point.getX();
        int y = point.getY();
        if (x >= rows) x = 0;
        if (y >= cols) y = 0;
        if (x < 0) x = rows - 1;
        if (y < 0) y = cols - 1;
        return new Point(x, y);
    }

    public Point getRandomPoint() {
        Random random = new Random();
        Point point;
        boolean canPut = false;
        do {
            point = new Point(random.nextInt(rows), random.nextInt(cols));
            for (Snake snake : snakes) {
                canPut |= point.equals(snake.getHead());
            }
            for (Food food : foods) {
                if (!food.isEaten()) {
                    canPut |= point.equals(food.getPoint());
                }
            }
        } while (canPut) ;
        return point;
    }

    public void update() {
        for(Snake snake : snakes) {
            for(Food food : foods) {
                if (food.getPoint().equals(snake.getHead())) {
                    snake.extend();
                    Point point = getRandomPoint();
                    food.setPoint(point);
                    cells[point.getX()][point.getY()] = Cell.FOOD;
                }
            }
            snake.move();
        }
    }

    public void updateRating(){
        TableView<RatingTableItems> rating = gameController.getRating();
        rating.getItems().removeAll();
        for(int i = 0; i < snakes.size(); i++){
            ObservableList<RatingTableItems> player = FXCollections.observableArrayList(
//                    new RatingTableItems(User.getInstance().getGameState().getPlayers().getPlayers(i).getName(), snakes.get(i).getPoints().size() * 10 - 20)
            );
            rating.setItems(player);
        }
    }


    public double getRows() {
        return rows;
    }

    public double getCols() {
        return cols;
    }

    public double getWidth() {
        return rows * cellSize;
    }

    public double getHeight() {
        return cols * cellSize;
    }

    public int getCellSize(){
        return cellSize;
    }

    public Cell[][] getCells(){
        return cells;
    }

    public Cell getCell(int x, int y){
        return cells[x][y];
    }

    public Collection<Snake> getSnakes() {
        return snakes;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public List<SnakesProto.GameState.Coord> foodsToCoords(){
        List<SnakesProto.GameState.Coord> coords = new Vector<>();
        for (Food food : foods) {
            coords.add(SnakesProto.GameState.Coord.newBuilder().setX(food.getPoint().getX()).setY(food.getPoint().getX()).build());
        }
        return coords;
    }

    public void setSnakes(Collection<Snake> snakes){
        this.snakes = snakes;
    }

    public void setFoods(List<Food> foods){
        this.foods = foods;
    }
}