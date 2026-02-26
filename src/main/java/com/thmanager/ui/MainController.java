package com.thmanager.ui;

import com.thmanager.dao.GameDAO;
import com.thmanager.model.Game;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

//UI总控制器类
public class MainController implements Initializable {
    @FXML
    private BorderPane mainContainer;
    @FXML
    private ListView<Game> gameListView;
    @FXML
    private VBox detailPanel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label infoLabel;
    @FXML
    private Button launchButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Label totalTimeLabel;
    @FXML
    private Label lastPlayedLabel;

    private GameDAO gameDAO;
    private ObservableList<Game> games;
    private Game selectedGame;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        gameDAO = new GameDAO();
        games = FXCollections.observableArrayList();

        setupGameList();
        loadGames();
        setupEventHandlers();
    }

    private void setupGameList(){
        //自定义单元格渲染
        gameListView.setCellFactory(lv -> new ListCell<Game>(){
            @Override
            protected void updateItem(Game game,boolean empty){
                super.updateItem(game,empty);
                if(empty || game == null){
                    setText(null);
                    setGraphic(null);
                }else{
                    HBox cell = new HBox(10);
                    cell.setStyle("-fx-padding:8px;-fx-alignment: center;");

                    //状态指示器
                    Label status = new Label(game.isInstalled() ? "●" : "○");
                    status.setStyle(game.isInstalled() ?
                            "-fx-text-fill:#4CAF50;-fx-font-size:12px;":
                            "-fx-text-fill:#999; -fx-font-size:12px;");

                    //游戏名称
                    Label name = new Label(game.getDisplayName());
                    name.setStyle("-fx-font-size:14px;");
                    if(!game.isInstalled()){
                        name.setStyle("-fx-font-size:14px;-fx-text-fill:#666;");
                    }

                    cell.getChildren().addAll(status, name);
                    setGraphic(cell);
                }
            }
        });

        //选择事件
        gameListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedGame = newValue;
                    updateDetailView();
                });
    }

    private void loadGames(){
        games.clear();
        games.addAll(gameDAO.findAll());
        gameListView.setItems(games);
    }

    private void updateDetailView(){
        if(selectedGame == null){
            detailPanel.setVisible(false);
            return ;
        }

        detailPanel.setVisible(true);
        titleLabel.setText(selectedGame.getDisplayName());

        StringBuilder info = new StringBuilder();
        info.append("日文名:").append(selectedGame.getTitleJa()).append("\n");
        info.append("中文名:").append(selectedGame.getTitleCn()).append("\n");
        info.append("状态:").append(selectedGame.isInstalled() ? "已安装" : "未安装").append("\n");

        if(selectedGame.getInstallPath() != null){
            info.append("路径:").append(selectedGame.getInstallPath()).append("\n");
        }

        infoLabel.setText(info.toString());

        //游玩时间
        if(selectedGame.getTotalPlayTimeSeconds() > 0){
            totalTimeLabel.setText("总游玩时间:" + selectedGame.getFormattedPlayTime());
            totalTimeLabel.setVisible(true);
        }else{
            totalTimeLabel.setVisible(false);
        }

        //最近一次游玩
        if(selectedGame.getLastPlayed() != null){
            lastPlayedLabel.setText("上次游玩：" + selectedGame.getLastPlayed().toLocalDate());
            lastPlayedLabel.setVisible(true);
        }else{
            lastPlayedLabel.setVisible(false);
        }

        //启动按钮
        launchButton.setDisable(!selectedGame.isInstalled());
        launchButton.setText(selectedGame.isInstalled() ? "启动游戏" : "未安装");
    }

    private void setupEventHandlers(){
        //启动按钮
        launchButton.setOnAction(event -> launchGame());

        //设置按钮
        settingsButton.setOnAction(event -> openSettings());

        //双击启动
        gameListView.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2 && selectedGame != null && selectedGame.isInstalled()){
                launchGame();
            }
        });
    }

    private void launchGame(){
        if(selectedGame == null || !selectedGame.isInstalled()){
            return ;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("启动游戏");
        alert.setHeaderText(null);
        alert.setContentText("即将启动:" + selectedGame.getDisplayName() + "\n路径:" + selectedGame.getFullExePath());
        alert.showAndWait();
    }

    private void openSettings(){
        try{
            FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/ui/settings_dialog.fxml"));

            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("游戏路径设置");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root,600,400));

            //传递数据并设置回调
            SettingsController controller = loader.getController();
            controller.setGames(games);
            controller.setOnSaveCallback(this::loadGames);

            dialogStage.showAndWait();
        }catch(IOException e){
            showError("无法打开设置",e.getMessage());
        }
    }

    private void showError(String title, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onExit(){
        Platform.exit();
    }
}
