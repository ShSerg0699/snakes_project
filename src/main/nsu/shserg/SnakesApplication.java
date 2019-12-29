package nsu.shserg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nsu.shserg.logic.Grid;

import java.net.URL;

public class SnakesApplication extends Application {

    private static Stage stage;
    private static Grid grid;

    @Override
    public void start(Stage primaryStage) throws Exception {
        setStage(primaryStage);
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource("/mainMenu.fxml");
        loader.setLocation(xmlUrl);
        Parent root = loader.load();

        primaryStage.setTitle("Snake");
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public void go(String[] args){
        launch(args);
    }

    public static Stage getStage() {
        return stage;
    }

    private void setStage(Stage stage) {
        SnakesApplication.stage = stage;
    }

    public static Grid getGrid(){
        return grid;
    }

    public static void setGrid(Grid grid){
        SnakesApplication.grid = grid;
    }

}
