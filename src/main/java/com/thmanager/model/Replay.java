package com.thmanager.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Replay信息实体类
@Data
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