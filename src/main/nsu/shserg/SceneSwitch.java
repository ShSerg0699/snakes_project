package nsu.shserg;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import nsu.shserg.characters.User;
import nsu.shserg.proto.SnakesProto;

import java.io.IOException;
import java.net.URL;

public class SceneSwitch {

    public Scene getScene(String path) {

        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL xmlUrl = getClass().getResource(path);
            loader.setLocation(xmlUrl);
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (path.equals("/gameField.fxml")) {
            Scene scene = new Scene(root);
            scene.setOnKeyPressed(e -> {
                SnakesProto.Direction direction;
                if ((direction = getDirection(e.getCode())) != null) {
                    User.getInstance().sendDirection(direction);
                }
            });
            return scene;
        } else {
            return new Scene(root);
        }
    }

    private SnakesProto.Direction getDirection(KeyCode keyCode){
        switch (keyCode){
            case UP:
                return SnakesProto.Direction.UP;
            case LEFT:
                return SnakesProto.Direction.LEFT;
            case RIGHT:
                return SnakesProto.Direction.RIGHT;
            case DOWN:
                return SnakesProto.Direction.DOWN;
            default:
                return null;
        }
    }

}
