package nsu.shserg.controllers;

import javafx.application.Platform;
import javafx.scene.Scene;
import nsu.shserg.SceneSwitch;
import nsu.shserg.SnakesApplication;
import nsu.shserg.characters.Master;
import nsu.shserg.characters.User;
import nsu.shserg.logic.GameLoop;
import nsu.shserg.logic.Grid;
import nsu.shserg.proto.SnakesProto;
import nsu.shserg.tablels_items.RatingTableItems;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import nsu.shserg.Painter;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class GameController {
    @FXML
    private TableView<RatingTableItems> rating;
    @FXML
    private TableColumn<RatingTableItems, String> name;
    @FXML
    private TableColumn<RatingTableItems, Integer> score;

    @FXML
    private Button newGameButton;
    @FXML
    private Button exitButton;
    @FXML
    private Canvas gameField;
    GameLoop loop;
    private GraphicsContext context;
    private static final int GRID_SIZE = 600;

    @FXML
    private void initialize() {
        newGameButton.setOnAction(event -> {

        });
        exitButton.setOnAction(event -> {
            SceneSwitch sceneSwitch = new SceneSwitch();
            Scene mainMenuScene = sceneSwitch.getScene("/mainMenu.fxml");
            SnakesApplication.getStage().setScene(mainMenuScene);
            loop.pause();
            if(User.getInstance().getPlayerId() == 0){
                Master.getInstance().stop();
            }
        });

        name.setCellValueFactory(new PropertyValueFactory<>("Name"));
        score.setCellValueFactory(new PropertyValueFactory<>("Score"));
        initCanvas(User.getInstance().getGameConfig());//get state message and add current table

       // + String.valueOf(User.getInstance().getGameConfig().getWidth()) + "x" +
               // String.valueOf(User.getInstance().getGameConfig().getHeight()));
        setNewRatingItems("", 0);
    }

    public void setNewRatingItems(String name, int score) {
        ObservableList<RatingTableItems> player = FXCollections.observableArrayList(
                new RatingTableItems(name, score)
        );
        rating.setItems(player);
    }

    public TableView<RatingTableItems> getRating() {
        return rating;
    }

    public void initCanvas(SnakesProto.GameConfig gameConfig) {
        int cellSize = getCellSize(gameConfig.getWidth(), gameConfig.getHeight());
        gameField.setWidth(cellSize * gameConfig.getWidth());
        gameField.setHeight(cellSize * gameConfig.getHeight());
        context = gameField.getGraphicsContext2D();
        gameField.setFocusTraversable(true);

        Grid grid = new Grid(cellSize * gameConfig.getWidth(), cellSize * gameConfig.getHeight(), cellSize, this);
        User.getInstance().setGrid(grid);
        SnakesApplication.setGrid(grid);
        if(User.getInstance().getPlayerId() == 0){
            Master.getInstance().addPlayer(User.getInstance().getPlayerName(), "localhost", 45455,SnakesProto.NodeRole.NORMAL);
            User.getInstance().setMasterAddress(new InetSocketAddress("localhost",54544));
//        }
//        if (User.getInstance().initSnake() == 1) {
//            SceneSwitch sceneSwitch = new SceneSwitch();
//            Scene joinToGameScene = sceneSwitch.getScene("/joinMenu.fxml");
//            SnakesApplication.getStage().setScene(joinToGameScene);
//        }
//        if (User.getInstance().getPlayerId() == 0){
            Master.getInstance().start();
        }
        loop = new GameLoop(grid, context);
        (new Thread(loop)).start();
        Painter.paint(grid, context);
    }

    public static int getCellSize(int width, int height) {
        int max = Math.max(width, height);
        return GRID_SIZE / max;
    }
}
