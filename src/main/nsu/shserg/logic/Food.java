package nsu.shserg.logic;

import javafx.scene.paint.Color;

public class Food {
    public static final Color COLOR = Color.YELLOWGREEN;
    private Point point;
    private boolean eaten = false;

    public Food(Point point) {
        this.point = point;
    }

    public Point getPoint() {
        return point;
    }

    public boolean isEaten() {
        return eaten;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}