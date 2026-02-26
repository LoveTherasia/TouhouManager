package com.thmanager;

import com.thmanager.dao.DatabaseManager;
import com.thmanager.dao.GameDAO;
import com.thmanager.model.Game;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        //加载FXML布局
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/main.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("TouHou Manager v0.2");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
