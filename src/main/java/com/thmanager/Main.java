package com.thmanager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage){
        Label label = new Label("东方启动器 - Touhou Manager\n项目初始化成功!");
        label.setStyle("-fx-font-size:18px; -fx-padding: 20px;");

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root,400,200);

        primaryStage.setTitle("Touhou Manager v0.1");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
