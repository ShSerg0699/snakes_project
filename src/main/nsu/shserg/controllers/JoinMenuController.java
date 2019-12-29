package nsu.shserg.controllers;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import nsu.shserg.SceneSwitch;
import nsu.shserg.SnakesApplication;
import nsu.shserg.characters.User;
import nsu.shserg.logic.Player;
import nsu.shserg.net.AnnouncementContext;
import nsu.shserg.net.MulticastListener;
import nsu.shserg.proto.SnakesProto;

import java.net.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JoinMenuController {
    @FXML
    TextField name;
    @FXML
    ListView<String> announcementGame;
    @FXML
    Button back;
    @FXML
    Button joinGame;
    @FXML
    Button update;
    private Map<SnakesProto.GameMessage.AnnouncementMsg, AnnouncementContext> availableGames = new ConcurrentHashMap<>();
    private Map<String, SnakesProto.GameMessage.AnnouncementMsg> forJoin = new ConcurrentHashMap<>();
    private ObservableList<String> items = FXCollections.observableArrayList();
    private boolean isItemSelected = false;
    private String selectedItemStr;
    private SnakesProto.GameMessage.AnnouncementMsg selectedItem;
    private InetSocketAddress masterAddress;
    private User user = User.getInstance();
    @FXML
    public void initialize(){
        try {
            MulticastListener multicastListener = new MulticastListener(InetAddress.getByName("239.192.0.4"), availableGames);
            multicastListener.run();
        } catch (UnknownHostException e){
            System.err.println(e.getLocalizedMessage());
        }

        SceneSwitch sceneSwitch = new SceneSwitch();
        announcementGame.setItems(items);
        announcementGame.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String old_val, String new_val) -> {
            isItemSelected =true;
            selectedItemStr = announcementGame.getSelectionModel().getSelectedItem();
            forJoin.forEach((k,v) ->{
                if(k.equals(selectedItemStr)){
                    selectedItem = v;
                }
            });
            availableGames.forEach((k,v) -> {
                if(selectedItem.equals(k)){
                    masterAddress = v.getMasterAddress();
                }
            });
        });
        update.setOnAction(actionEvent -> {
            items.removeAll();
            availableGames.forEach((k,v) ->{
                setItems(k, v);
            });
        });
        joinGame.setOnAction(actionEvent -> {
            if(!isItemSelected){
                return;
            }
            user.setGameConfig(selectedItem.getConfig());
            user.setPlayerName(name.getText());
            user.setPlayerId(selectedItem.getPlayers().getPlayersCount());
            user.setMasterAddress(masterAddress);
            user.sendJoinMsg(masterAddress, name.getText());
            Scene gameFieldScene = sceneSwitch.getScene("/gameField.fxml");
            SnakesApplication.getStage().setScene(gameFieldScene);
        });
        back.setOnAction(actionEvent -> {
            Scene mainMenuScene = sceneSwitch.getScene("/mainMenu.fxml");
            SnakesApplication.getStage().setScene(mainMenuScene);
        });
    }

    private void setItems(SnakesProto.GameMessage.AnnouncementMsg gameMes, AnnouncementContext context){
        String itemText = context.getMasterAddress().getHostString() + "," + Integer.toString(gameMes.getConfig().getHeight()) + "x" + Integer.toString(gameMes.getConfig().getWidth()) +
                  "," + Integer.toString(gameMes.getConfig().getFoodStatic()) + "+" + Integer.toString(gameMes.getPlayers().getPlayersCount()) + "*" + Float.toString(gameMes.getConfig().getFoodPerPlayer());
        items.add(itemText);
        if(!forJoin.containsKey(itemText)) {
            forJoin.put(itemText, gameMes);
        }
    }


}
