package com.thmanager.ui;

import com.thmanager.dao.GameDAO;
import com.thmanager.model.Game;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private TableView<Game> gameTable;
    @FXML private TableColumn<Game, String> numberColumn;
    @FXML private TableColumn<Game, String> nameColumn;
    @FXML private TableColumn<Game, String> pathColumn;
    @FXML private TableColumn<Game, Boolean> statusColumn;

    private GameDAO gameDAO;
    private ObservableList<Game> games;
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameDAO = new GameDAO();

        // 设置表格列
        numberColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("TH%02d", data.getValue().getGameNumber())
                ));

        nameColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDisplayName()));

        pathColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getInstallPath() != null ? data.getValue().getInstallPath() : "未设置"
                ));

        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleBooleanProperty(data.getValue().isInstalled()));

        statusColumn.setCellFactory(col -> new TableCell<Game, Boolean>() {
            @Override
            protected void updateItem(Boolean installed, boolean empty) {
                super.updateItem(installed, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(installed ? "✓" : "✗");
                    setStyle(installed ? "-fx-text-fill: green; -fx-alignment: center;" :
                            "-fx-text-fill: red; -fx-alignment: center;");
                }
            }
        });

        // 双击编辑路径
        gameTable.setRowFactory(tv -> {
            TableRow<Game> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editGamePath(row.getItem());
                }
            });
            return row;
        });
    }

    public void setGames(ObservableList<Game> games) {
        this.games = games;
        gameTable.setItems(games);
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void onAutoDetect() {
        // 自动检测常见安装路径
        String[] commonPaths = {
                "C:\\Program Files (x86)\\Steam\\steamapps\\common",
                "C:\\Program Files\\Steam\\steamapps\\common",
                System.getProperty("user.home") + "\\Games",
                System.getProperty("user.home") + "\\Desktop"
        };

        int found = 0;
        for (String basePath : commonPaths) {
            if (!Files.exists(Paths.get(basePath))) continue;

            for (Game game : games) {
                if (game.isInstalled()) continue; // 跳过已安装的

                // 尝试查找游戏文件夹
                String[] possibleFolders = {
                        String.format("th%02d", game.getGameNumber()),
                        game.getTitleEn(),
                        "Touhou " + game.getGameNumber()
                };

                for (String folder : possibleFolders) {
                    Path gamePath = Paths.get(basePath, folder);
                    if (Files.exists(gamePath)) {
                        Path exePath = gamePath.resolve(game.getExeName());
                        if (Files.exists(exePath)) {
                            setGamePath(game, gamePath.toString());
                            found++;
                            break;
                        }
                    }
                }
            }
        }

        showInfo("自动检测完成", "找到并配置了 " + found + " 个游戏");
        gameTable.refresh();
    }

    @FXML
    private void onBrowse() {
        Game selected = gameTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("请先选择游戏");
            return;
        }
        editGamePath(selected);
    }

    private void editGamePath(Game game) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择 " + game.getDisplayName() + " 的安装文件夹");

        if (game.getInstallPath() != null) {
            File initial = new File(game.getInstallPath());
            if (initial.exists()) chooser.setInitialDirectory(initial);
        }

        File selected = chooser.showDialog(gameTable.getScene().getWindow());
        if (selected != null) {
            setGamePath(game, selected.getAbsolutePath());
            gameTable.refresh();
        }
    }

    private void setGamePath(Game game, String path) {
        // 验证路径有效性
        Path exePath = Paths.get(path, game.getExeName());
        boolean valid = Files.exists(exePath);

        game.setInstallPath(path);
        game.setInstalled(valid);

        // 推断replay路径
        Path replayPath = Paths.get(path, "replay");
        if (Files.exists(replayPath)) {
            game.setReplayFolder(replayPath.toString());
        }

        // 保存到数据库
        gameDAO.update(game);
    }

    @FXML
    private void onSave() {
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        closeDialog();
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) gameTable.getScene().getWindow()).close();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}