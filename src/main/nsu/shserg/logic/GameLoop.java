package nsu.shserg.logic;

import javafx.application.Platform;
import nsu.shserg.Painter;
import javafx.scene.canvas.GraphicsContext;
import nsu.shserg.characters.User;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class GameLoop implements Runnable {
    private final Grid grid;
    private final GraphicsContext context;
    private float stateDelayMs;
    private boolean paused;


    public GameLoop(final Grid grid, final GraphicsContext context) {
        this.grid = grid;
        this.context = context;

        stateDelayMs = User.getInstance().getGameConfig().getStateDelayMs();
        paused = false;

    }

    @Override
    public void run() {
        while (!paused) {
            // Time the update and paint calls
            float time = System.currentTimeMillis();


            grid.update();
            grid.updateRating();
            Painter.paint(grid, context);

            //need to rewrite, because the game will stop after the death of one of the snakes
            Collection<Snake> snakes = grid.getSnakes();
            for (Snake snake : snakes) {
                if (!snake.isSafe()) {
                    Random random = new Random();
                    for (Point point : snake.getPoints()) {
                        if (random.nextDouble() < User.getInstance().getGameConfig().getDeadFoodProb()) {
                            grid.getFoods().add(new Food(point));
                        }
                    }
                    snakes.remove(snake);
                    Painter.paint(grid, context);
                    pause();
                    Painter.paintResetMessage(context);
                    break;
                }
            }

            time = System.currentTimeMillis() - time;

            // Adjust the timing correctly
            if (time < stateDelayMs) {
                try {
                    Thread.sleep((long) (stateDelayMs - time));
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    public void pause() {
        paused = true;
    }
}