package nsu.shserg.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import nsu.shserg.SceneSwitch;
import nsu.shserg.SnakesApplication;


public class MainMenuController {
    @FXML
    private Button createGame;
    @FXML
    private Button joinGame;
    @FXML
    private Button quit;

    @FXML
    private void initialize(){
        SceneSwitch sceneSwitch = new SceneSwitch();
        createGame.setOnAction(actionEvent -> {
            Scene createGameScene = sceneSwitch.getScene("/createMenu.fxml");
            SnakesApplication.getStage().setScene(createGameScene);
        });
        joinGame.setOnAction(actionEvent -> {
            Scene joinToGameScene = sceneSwitch.getScene("/joinMenu.fxml");
            SnakesApplication.getStage().setScene(joinToGameScene);
        });
        quit.setOnAction(actionEvent -> {
            SnakesApplication.getStage().close();
            System.exit(0);
        });
    }
}
