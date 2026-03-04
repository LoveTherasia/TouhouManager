package com.thmanager.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Replay {

    // 数据库基础字段
    private int id;
    private int gameId;
    private String fileName;
    private String filePath;
    private long fileSize;
    private long fileModifiedTime;
    private LocalDateTime importedAt;
    private Integer sessionId;

    // 游戏信息字段
    private String gameVersion;
    private String character;
    private String shotType;
    private String difficulty;
    private String stage;
    private boolean cleared;
    private long totalScore;
    private LocalDateTime gameDate;
    private String playerName;
    private float slowRate;
    private int totalFrames;

    // 深度统计字段（JSON存储）
    private String stageScoresJson;
    private String bombStatsJson;
    private int totalZBombs;
    private int totalXBombs;
    private int totalCBombs;

    // 原始数据备份
    private String rawJson;

    // 显示用临时字段（非数据库）
    private String gameTitle;
    private List<Long> stageScoresList;
    private List<StageBombStats> bombStatsList;

    // 错误信息
    private List<String> errors = new ArrayList<>();

    public Replay() {
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    // ========== 业务方法（新增！）==========

    /**
     * 获取显示用的完整机体名
     */
    public String getFullShotType() {
        if (character == null)
            return "Unknown";
        if (shotType == null || shotType.isEmpty())
            return character;
        return character + " " + shotType;
    }

    /**
     * 格式化分数显示
     */
    public String getFormattedScore() {
        return String.format("%,d", totalScore);
    }

    /**
     * 获取难度简写（新增！）
     */
    public String getDifficultyDisplay() {
        if (difficulty == null)
            return "?";
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> "E";
            case "NORMAL" -> "N";
            case "HARD" -> "H";
            case "LUNATIC" -> "L";
            case "EXTRA" -> "Ex";
            case "PHANTASM" -> "Ph";
            default -> difficulty.substring(0, Math.min(1, difficulty.length()));
        };
    }

    /**
     * 获取到达面数字
     */
    public int getReachedStageNumber() {
        if (stage == null)
            return 0;
        String num = stage.replaceAll("[^0-9]", "");
        if (!num.isEmpty()) {
            try {
                return Integer.parseInt(num);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        if (stage.contains("Extra"))
            return 7;
        if (stage.contains("Phantasm"))
            return 8;
        if (stage.contains("All") || stage.contains("Clear"))
            return 99;
        return 0;
    }

    /**
     * 计算某面的分数
     */
    public long getStageScore(int stageNum) {
        if (stageScoresList == null || stageNum < 1 || stageNum > stageScoresList.size()) {
            return 0;
        }
        return stageScoresList.get(stageNum - 1);
    }

    /**
     * 计算某面的炸弹使用次数
     */
    public int getStageBombCount(int stageNum) {
        if (bombStatsList == null || stageNum < 1 || stageNum > bombStatsList.size()) {
            return 0;
        }
        return bombStatsList.get(stageNum - 1).xCount;
    }

    // ========== Getters and Setters ==========

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileModifiedTime() {
        return fileModifiedTime;
    }

    public void setFileModifiedTime(long fileModifiedTime) {
        this.fileModifiedTime = fileModifiedTime;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getShotType() {
        return shotType;
    }

    public void setShotType(String shotType) {
        this.shotType = shotType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(long totalScore) {
        this.totalScore = totalScore;
    }

    public LocalDateTime getGameDate() {
        return gameDate;
    }

    public void setGameDate(LocalDateTime gameDate) {
        this.gameDate = gameDate;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public float getSlowRate() {
        return slowRate;
    }

    public void setSlowRate(float slowRate) {
        this.slowRate = slowRate;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public void setTotalFrames(int totalFrames) {
        this.totalFrames = totalFrames;
    }

    public String getStageScoresJson() {
        return stageScoresJson;
    }

    public void setStageScoresJson(String stageScoresJson) {
        this.stageScoresJson = stageScoresJson;
        if (stageScoresJson != null && !stageScoresJson.isEmpty()) {
            try {
                this.stageScoresList = new ArrayList<>();
                String clean = stageScoresJson.replace("[", "").replace("]", "").trim();
                if (!clean.isEmpty()) {
                    String[] parts = clean.split(",");
                    for (String part : parts) {
                        this.stageScoresList.add(Long.parseLong(part.trim()));
                    }
                }
            } catch (Exception e) {
                this.stageScoresList = null;
            }
        }
    }

    public String getBombStatsJson() {
        return bombStatsJson;
    }

    public void setBombStatsJson(String bombStatsJson) {
        this.bombStatsJson = bombStatsJson;
    }

    public int getTotalZBombs() {
        return totalZBombs;
    }

    public void setTotalZBombs(int totalZBombs) {
        this.totalZBombs = totalZBombs;
    }

    public int getTotalXBombs() {
        return totalXBombs;
    }

    public void setTotalXBombs(int totalXBombs) {
        this.totalXBombs = totalXBombs;
    }

    public int getTotalCBombs() {
        return totalCBombs;
    }

    public void setTotalCBombs(int totalCBombs) {
        this.totalCBombs = totalCBombs;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public List<Long> getStageScoresList() {
        return stageScoresList;
    }

    public void setStageScoresList(List<Long> stageScoresList) {
        this.stageScoresList = stageScoresList;
    }

    public List<StageBombStats> getBombStatsList() {
        return bombStatsList;
    }

    public void setBombStatsList(List<StageBombStats> bombStatsList) {
        this.bombStatsList = bombStatsList;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s",
                gameTitle != null ? gameTitle : "Unknown",
                getFullShotType(),
                getDifficultyDisplay(),
                getFormattedScore(),
                cleared ? "✓" : "");
    }

    // 内部类：每面炸弹统计
    public static class StageBombStats {
        public int stageNum;
        public int zCount;
        public int xCount;
        public int startFrame;
        public int endFrame;
    }
}