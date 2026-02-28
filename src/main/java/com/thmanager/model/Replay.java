package com.thmanager.model;

import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;

//回放实体类
public class Replay {
    private int id;//数据库id
    private int gameId;//关联游戏Id
    private String fileName;//文件名
    private String filePath;//完整路径
    private long fileSize;//文件大小

    //从.rpy文件解析的信息
    private String gameVersion;//游戏版本
    private String difficulty;//游戏难度
    private String shotType;//机体
    private long score;//分数
    private LocalDateTime date;//游玩日期
    private String stage;//到达面数
    private float slowRate;
    private String playerName;//玩家名
    private int frameCount;        // 新增：总帧数
    private String rawJson;        // 新增：原始JSON数据

    private String gameTitle;

    // 错误信息（解析过程中发现的问题）
    private List<String> errors = new ArrayList<>();

    public Replay(){}

    //格式化分数显示
    public String getFormattedScore(){
        return String.format("%,d",score);
    }

    // 获取难度显示（带颜色代码，供UI使用）
    public String getDifficultyDisplay() {
        if (difficulty == null) return "Unknown";
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> "E";
            case "NORMAL" -> "N";
            case "HARD" -> "H";
            case "LUNATIC" -> "L";
            case "EXTRA" -> "Ex";
            case "PHANTASM" -> "Ph";
            default -> difficulty;
        };
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getGameVersion() { return gameVersion; }
    public void setGameVersion(String gameVersion) { this.gameVersion = gameVersion; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getShotType() { return shotType; }
    public void setShotType(String shotType) { this.shotType = shotType; }

    public long getScore() { return score; }
    public void setScore(long score) { this.score = score; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public float getSlowRate() { return slowRate; }
    public void setSlowRate(float slowRate) { this.slowRate = slowRate; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    // 新增：frameCount 的 getter 和 setter
    public int getFrameCount() { return frameCount; }
    public void setFrameCount(int frameCount) { this.frameCount = frameCount; }

    // 新增：rawJson 的 getter 和 setter
    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }

    // ========== 错误信息相关方法 ==========
    // addError方法，解决解析失败问题
    public void addError(String errorMsg) {
        this.errors.add(errorMsg);
    }

    // 获取所有错误信息
    public List<String> getErrors() {
        return new ArrayList<>(this.errors); // 返回副本，避免外部修改
    }

    // 判断是否有解析错误
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s",
                gameTitle != null ? gameTitle : "Unknown",
                shotType != null ? shotType : "?",
                getDifficultyDisplay(),
                getFormattedScore());
    }
}