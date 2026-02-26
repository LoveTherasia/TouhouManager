package com.thmanager.ui;

import com.thmanager.dao.GameDAO;
import com.thmanager.model.Game;
import com.thmanager.service.GameLauncher;
import javafx.animation.Timeline;
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
    @FXML private BorderPane mainContainer;//主面板
    @FXML private ListView<Game> gameListView;//游戏列表
    @FXML private VBox detailPanel;//游戏详情面板
    @FXML private Label titleLabel;//游戏标题
    @FXML private Label infoLabel;//游戏信息
    @FXML private Button launchButton;//启动按钮
    @FXML private Button settingsButton;//设置按钮
    @FXML private Label totalTimeLabel;//事件按钮
    @FXML private Label lastPlayedLabel;//上次游玩时间
    @FXML private Label statusLabel;//底部状态栏

    private GameDAO gameDAO;
    private GameLauncher gameLauncher;
    private ObservableList<Game> games;
    private Game selectedGame;
    private Timeline countdownTimeLine;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        gameDAO = new GameDAO();
        gameLauncher = new GameLauncher();

        // 设置游戏启动回调
        gameLauncher.setOnGameStart(() ->{
            updateStatus("少女祈祷中...");
            launchButton.setDisable(true);
            launchButton.setText("运行中...");
        });

        // 设置游戏结束回调
        gameLauncher.setOnGameEnd(() -> {
            updateStatus("游戏已结束，数据已保存");
            refreshGameList();
            updateDetailPanel();
            launchButton.setDisable(false);
            launchButton.setText("启动游戏");
        });

        setupGameList();
        loadGames();
        setupEventHandlers();
        updateStatus("就绪");
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

                    //如果正在运行，显示指示器
                    if(gameLauncher.isGameRunning() &&
                       gameLauncher.getCurrentGame().isPresent() &&
                       gameLauncher.getCurrentGame().get().getId() == game.getId())
                    {
                        Label running = new Label(" ▶");
                        running.setStyle("-fx-text-fill: #f44336;");
                        cell.getChildren().addAll(status, name, running);
                    } else {
                        cell.getChildren().addAll(status, name);
                    }
                    setGraphic(cell);
                }
            }
        });

        //选择事件
        gameListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    selectedGame = newValue;
                    updateDetailPanel();
                });
    }

    private void loadGames(){
        games = FXCollections.observableArrayList(gameDAO.findAll());
        gameListView.setItems(games);
    }

    private void refreshGameList(){
        games.clear();
        games.addAll(gameDAO.findAll());
        gameListView.refresh();
    }

    private void updateDetailPanel(){
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

        //检查是否正在运行
        boolean isRunning = gameLauncher.isGameRunning() &&
                gameLauncher.getCurrentGame().isPresent() &&
                gameLauncher.getCurrentGame().get().getId() == selectedGame.getId();

        launchButton.setDisable(!selectedGame.isInstalled()||
                (gameLauncher.isGameRunning() && !isRunning));

        if(isRunning){
            launchButton.setText("运行中...");
        }else{
            launchButton.setText(selectedGame.isInstalled() ? "启动游戏" : "未安装");
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

    //带倒计时的启动
    private void startGameWithCountdown(){
        if(selectedGame == null || !selectedGame.isInstalled()) return;

        //倒计时
        launchButton.setDisable(true);

        gameLauncher.launchWithCountdown(selectedGame,3,new GameLauncher.CountdownCallback(){
            @Override
            public void onTick(int seconds){
                launchButton.setText("启动中..." + seconds);
                updateStatus("游戏将在" + seconds +"秒后启动");
            }

            @Override
            public void onFinish(){

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

        //关键：调用倒计时启动方法
        startGameWithCountdown();
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

    private void updateStatus(String text){
        if(statusLabel != null){
            statusLabel.setText(text);
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
