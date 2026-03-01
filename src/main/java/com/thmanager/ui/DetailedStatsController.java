package com.thmanager.ui;

import com.thmanager.dao.GameDAO;
import com.thmanager.model.Game;
import com.thmanager.service.ReplayStatisticsService;
import com.thmanager.service.ReplayStatisticsService.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class DetailedStatsController implements Initializable {

    @FXML private ComboBox<Game> gameSelector;
    @FXML private ComboBox<String> difficultySelector;

    @FXML private Label bestScoreLabel;
    @FXML private Label bestShotLabel;
    @FXML private Label bestStageLabel;

    @FXML private TableView<ShotTypeUsage> shotTable;
    @FXML private TableView<StageBombStat> bombTable;
    @FXML private Label clearRateLabel;

    private ReplayStatisticsService statsService;
    private GameDAO gameDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statsService = new ReplayStatisticsService();
        gameDAO = new GameDAO();

        setupSelectors();
        setupTables();
    }

    private void setupSelectors() {
        gameSelector.setItems(FXCollections.observableArrayList(gameDAO.findInstalled()));
        difficultySelector.setItems(FXCollections.observableArrayList(
                "Easy", "Normal", "Hard", "Lunatic", "Extra", "Phantasm"
        ));

        Runnable update = () -> {
            if (gameSelector.getValue() != null && difficultySelector.getValue() != null) {
                loadStats(gameSelector.getValue().getId(), difficultySelector.getValue());
            }
        };

        gameSelector.setOnAction(e -> update.run());
        difficultySelector.setOnAction(e -> update.run());
    }

    private void setupTables() {
        TableColumn<ShotTypeUsage, String> shotCol = new TableColumn<>("机体");
        shotCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(d.getValue().getShotType()));

        TableColumn<ShotTypeUsage, String> countCol = new TableColumn<>("次数");
        countCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getUseCount())));

        TableColumn<ShotTypeUsage, String> bestCol = new TableColumn<>("最高分");
        bestCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%,d", d.getValue().getBestScore())));

        shotTable.getColumns().addAll(shotCol, countCol, bestCol);

        TableColumn<StageBombStat, String> stageCol = new TableColumn<>("面数");
        stageCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty("Stage " + d.getValue().getStageNumber()));

        TableColumn<StageBombStat, String> xBombCol = new TableColumn<>("平均炸弹(X)");
        xBombCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f", d.getValue().getAvgXBombs())));

        bombTable.getColumns().addAll(stageCol, xBombCol);
    }

    private void loadStats(int gameId, String difficulty) {
        DifficultyFullReport report = statsService.generateDifficultyReport(gameId, difficulty);
        if (report == null) return;

        if (report.getBestRecord() != null) {
            bestScoreLabel.setText(report.getBestRecord().getFormattedScore());
            bestShotLabel.setText(report.getBestRecord().getFullShotType());
            bestStageLabel.setText(report.getBestRecord().getStage());
        }

        shotTable.setItems(FXCollections.observableArrayList(report.getShotUsage()));
        bombTable.setItems(FXCollections.observableArrayList(report.getBombStats()));
        clearRateLabel.setText(String.format("通关率: %.1f%%", report.getOverallClearRate()));
    }
}