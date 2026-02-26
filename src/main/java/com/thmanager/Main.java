package com.thmanager;

import com.thmanager.dao.DatabaseManager;
import com.thmanager.dao.GameDAO;
import com.thmanager.model.Game;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage){
        //初始化数据库
        DatabaseManager dbManager = DatabaseManager.getInstance();

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("东方启动器 - 数据库测试");
        titleLabel.setStyle("-fx-font-size:10px; -fx-font-weight:bold;");

        //数据库状态
        boolean dbOK = dbManager.testConnection();
        Label statusLabel = new Label("数据库状态:" + (dbOK ? "√ 连接成功" : "× 连接失败"));
        statusLabel.setStyle(dbOK ? "-fx-text-fill: green;" : "-fx-text-fill: red;");

        //加载游戏列表
        ListView<String> gameListView = new ListView<>();
        gameListView.setPrefHeight(300);

        try{
            GameDAO gameDAO = new GameDAO();
            List<Game> games = gameDAO.findAll();

            for(Game game : games){
                String display = String.format("TH%02d [%s] [%s] %s",
                        game.getGameNumber(),
                        game.getTitleEn(),
                        game.getTitleCn(),
                        game.isInstalled() ? "√" : "o");
                gameListView.getItems().add(display);
            }

            Label countLabel = new Label("共加载" + games.size() + "作");

            root.getChildren().addAll(titleLabel, statusLabel, new Label("游戏列表:"),gameListView,countLabel);
        }catch(Exception e){
            Label errorLabel = new Label("加载失败" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            root.getChildren().addAll(titleLabel,statusLabel,errorLabel);
        }

        Scene scene = new Scene(root,400,450);
        primaryStage.setTitle("Tou Hou Manager - Phase 1 Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
