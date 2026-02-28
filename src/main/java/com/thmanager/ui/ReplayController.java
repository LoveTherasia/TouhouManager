package com.thmanager.ui;

import com.thmanager.model.Replay;
import com.thmanager.service.ReplayScanner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ReplayController implements Initializable {

    @FXML private TableView<Replay> replayTable;
    @FXML private TableColumn<Replay, String> gameColumn;
    @FXML private TableColumn<Replay, String> dateColumn;
    @FXML private TableColumn<Replay, String> difficultyColumn;
    @FXML private TableColumn<Replay, String> shotColumn;
    @FXML private TableColumn<Replay, String> scoreColumn;
    @FXML private TableColumn<Replay, String> stageColumn;

    @FXML private ComboBox<String> gameFilter;
    @FXML private ComboBox<String> difficultyFilter;
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private Button playButton;
    @FXML private Label countLabel;

    private ReplayScanner scanner;
    private ObservableList<Replay> allReplays;
    private FilteredList<Replay> filteredReplays;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scanner = new ReplayScanner();
        allReplays = FXCollections.observableArrayList();
        filteredReplays = new FilteredList<>(allReplays, p -> true);

        setupTable();
        setupFilters();
        loadReplays();
    }

    private void setupTable() {
        gameColumn.setCellValueFactory(new PropertyValueFactory<>("gameTitle"));
        dateColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDate() != null ?
                                data.getValue().getDate().toLocalDate().toString() : "Unknown"
                ));
        difficultyColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDifficultyDisplay()
                ));
        shotColumn.setCellValueFactory(new PropertyValueFactory<>("shotType"));
        scoreColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFormattedScore()
                ));
        stageColumn.setCellValueFactory(new PropertyValueFactory<>("stage"));

        replayTable.setItems(filteredReplays);

        // 选择监听
        replayTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> playButton.setDisable(newVal == null)
        );

        // 双击播放
        replayTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                playSelectedReplay();
            }
        });
    }

    private void setupFilters() {
        // 游戏筛选
        gameFilter.setOnAction(e -> applyFilters());

        // 难度筛选
        difficultyFilter.getItems().addAll("全部", "E", "N", "H", "L", "Ex", "Ph");
        difficultyFilter.getSelectionModel().selectFirst();
        difficultyFilter.setOnAction(e -> applyFilters());

        // 搜索框
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());

        refreshButton.setOnAction(e -> loadReplays());
        playButton.setOnAction(e -> playSelectedReplay());
    }

    private void applyFilters() {
        String gameFilterVal = gameFilter.getValue();
        String diffFilterVal = difficultyFilter.getValue();
        String searchVal = searchField.getText().toLowerCase();

        filteredReplays.setPredicate(replay -> {
            boolean matchGame = gameFilterVal == null ||
                    gameFilterVal.equals("全部") ||
                    replay.getGameTitle().equals(gameFilterVal);

            boolean matchDiff = diffFilterVal == null ||
                    diffFilterVal.equals("全部") ||
                    replay.getDifficultyDisplay().equals(diffFilterVal);

            boolean matchSearch = searchVal.isEmpty() ||
                    replay.getFileName().toLowerCase().contains(searchVal) ||
                    (replay.getShotType() != null &&
                            replay.getShotType().toLowerCase().contains(searchVal));

            return matchGame && matchDiff && matchSearch;
        });

        updateCount();
    }

    private void loadReplays() {
        System.out.println("Loading replays...");
        refreshButton.setText("扫描中...");
        refreshButton.setDisable(true);

        // 在后台线程扫描
        new Thread(() -> {
            List<Replay> replays = scanner.scanAllGames();

            javafx.application.Platform.runLater(() -> {
                allReplays.clear();
                allReplays.addAll(replays);

                // 更新游戏筛选器
                List<String> games = replays.stream()
                        .map(Replay::getGameTitle)
                        .distinct()
                        .sorted()
                        .collect(java.util.stream.Collectors.toList());
                games.add(0, "全部");
                gameFilter.getItems().setAll(games);
                gameFilter.getSelectionModel().selectFirst();

                applyFilters();
                refreshButton.setText("刷新");
                refreshButton.setDisable(false);
            });
        }).start();
        System.out.println("Loading replays... done");
    }

    private void playSelectedReplay() {
        Replay selected = replayTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean success = scanner.playReplay(selected);
        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("播放失败");
            alert.setHeaderText(null);
            alert.setContentText("无法播放该replay，请检查游戏路径是否正确");
            alert.showAndWait();
        }
    }

    private void updateCount() {
        countLabel.setText(String.format("显示 %d / %d 个replay",
                filteredReplays.size(), allReplays.size()));
    }
}