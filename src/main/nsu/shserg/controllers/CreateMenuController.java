package nsu.shserg.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import nsu.shserg.SceneSwitch;
import nsu.shserg.SnakesApplication;
import nsu.shserg.characters.Master;
import nsu.shserg.characters.User;
import nsu.shserg.proto.SnakesProto;

public class CreateMenuController {
    @FXML
    private TextField width;
    @FXML
    private TextField height;
    @FXML
    private TextField foodStatic;
    @FXML
    private TextField foodPerPlayer;
    @FXML
    private TextField stateDelayMs;
    @FXML
    private TextField deadFoodProb;
    @FXML
    private TextField pingDelayMs;
    @FXML
    private TextField nodeTimeoutMs;
    @FXML
    private TextField name;
    @FXML
    private Button setConfig;
    @FXML
    private Button back;

    @FXML
    private void initialize(){
        SceneSwitch sceneSwitch = new SceneSwitch();
        setConfig.setOnAction(actionEvent -> {
            Master master= Master.getInstance();
            SnakesProto.GameConfig config = initConfig();
            master.setGameConfig(config);
            User.getInstance().setPlayerId(0);
            User.getInstance().setPlayerName(name.getText());
            Scene gameFieldScene = sceneSwitch.getScene("/gameField.fxml");
            SnakesApplication.getStage().setScene(gameFieldScene);
        });
        back.setOnAction(actionEvent -> {
            Scene mainMenuScene = sceneSwitch.getScene("/mainMenu.fxml");
            SnakesApplication.getStage().setScene(mainMenuScene);
        });
    }

    private SnakesProto.GameConfig initConfig(){
        return SnakesProto.GameConfig.newBuilder()
                .setWidth(Integer.parseInt(width.getText()))
                .setHeight(Integer.parseInt(height.getText()))
                .setFoodStatic(Integer.parseInt(foodStatic.getText()))
                .setFoodPerPlayer(Float.parseFloat(foodPerPlayer.getText()))
                .setStateDelayMs(Integer.parseInt(stateDelayMs.getText()))
                .setDeadFoodProb(Float.parseFloat(deadFoodProb.getText()))
                .setPingDelayMs(Integer.parseInt(pingDelayMs.getText()))
                .setNodeTimeoutMs(Integer.parseInt(nodeTimeoutMs.getText()))
                .build();
    }

}
