package nsu.shserg;

import javafx.application.Platform;
import nsu.shserg.logic.Food;
import nsu.shserg.logic.Grid;
import nsu.shserg.logic.Point;
import nsu.shserg.logic.Snake;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Collection;
import java.util.List;

public class Painter {

    private static int cellSize;

    public static void paint(Grid grid, GraphicsContext gc) {
        Platform.runLater(()-> {
            cellSize = grid.getCellSize();
            gc.setFill(Grid.COLOR);
            gc.fillRect(0, 0, grid.getWidth(), grid.getHeight());

            // Now the Food
            gc.setFill(Food.COLOR);
            List<Food> foods = grid.getFoods();
            for (Food food : foods) {
                paintPoint(food.getPoint(), gc);
            }
            // Now the snake

            Collection<Snake> snakes = grid.getSnakes();
            for (Snake snake : snakes) {
                gc.setFill(Snake.COLOR);
                snake.getPoints().forEach(point -> paintPoint(point, gc));
                if (!snake.isSafe()) {
                    gc.setFill(Snake.DEAD);
                    paintPoint(snake.getHead(), gc);
                }
            }
        });
    }

    private static void paintPoint(Point point, GraphicsContext gc) {
        gc.fillRect(point.getX() * cellSize, point.getY() * cellSize, cellSize, cellSize);
    }

    public static void paintResetMessage(GraphicsContext gc) {
        gc.setFill(Color.AQUAMARINE);
        gc.fillText("Hit RETURN to reset.", 10, 10);
    }
}