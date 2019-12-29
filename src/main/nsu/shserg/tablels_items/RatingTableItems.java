package nsu.shserg.tablels_items;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class RatingTableItems {
    private SimpleStringProperty name;
    private SimpleIntegerProperty score;

    public RatingTableItems(String name, int score){
        this.name = new SimpleStringProperty(name);
        this.score = new SimpleIntegerProperty(score);
    }


    public int getScore() {
        return score.get();
    }

    public void setScore(int score) {
        this.score = new SimpleIntegerProperty(score);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name= new SimpleStringProperty(name);
    }
}
