package com.thmanager.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Replay数据模型类
 * 
 * 该类表示一条东方Project游戏的录像记录，包含Replay文件信息、游戏详情、
 * 得分数据等。对应数据库中的 replays 表。
 */
@Data
@TableName("replays")
public class Replay {

    /**
     * Replay主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private int id;

    /**
     * 关联的游戏ID
     */
    @TableField("game_id")
    private int gameId;

    /**
     * Replay文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * Replay文件完整路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * Replay文件大小（字节）
     */
    @TableField("file_size")
    private long fileSize;

    /**
     * Replay文件最后修改时间戳
     */
    @TableField("file_modified_time")
    private long fileModifiedTime;

    /**
     * Replay导入到系统的时间
     */
    @TableField("imported_at")
    private LocalDateTime importedAt;

    /**
     * 关联的游戏会话ID
     */
    @TableField("session_id")
    private Integer sessionId;

    /**
     * 游戏版本号
     */
    @TableField("game_version")
    private String gameVersion;

    /**
     * 使用的角色
     */
    @TableField("character")
    private String character;

    /**
     * 使用的自机类型（子弹类型）
     */
    @TableField("shot_type")
    private String shotType;

    /**
     * 游戏难度（Easy/Normal/Hard/Lunatic/Extra/Phantasm）
     */
    @TableField("difficulty")
    private String difficulty;

    /**
     * 到达的关卡
     */
    @TableField("stage")
    private String stage;

    /**
     * 是否通关
     */
    @TableField("cleared")
    private boolean cleared;

    /**
     * 总得分
     */
    @TableField("total_score")
    private long totalScore;

    /**
     * 游戏日期时间
     */
    @TableField("game_date")
    private LocalDateTime gameDate;

    /**
     * 玩家名称
     */
    @TableField("player_name")
    private String playerName;

    /**
     * 慢帧率（Slow Rate）百分比
     */
    @TableField("slow_rate")
    private float slowRate;

    /**
     * 总游戏帧数
     */
    @TableField("total_frames")
    private int totalFrames;

    /**
     * 各关卡得分（JSON格式存储）
     */
    @TableField("stage_scores_json")
    private String stageScoresJson;

    /**
     * 炸弹使用统计（JSON格式存储）
     */
    @TableField("bomb_stats_json")
    private String bombStatsJson;

    /**
     * Z键炸弹使用总数
     */
    @TableField("total_z_bombs")
    private int totalZBombs;

    /**
     * X键炸弹使用总数
     */
    @TableField("total_x_bombs")
    private int totalXBombs;

    /**
     * C键炸弹使用总数
     */
    @TableField("total_c_bombs")
    private int totalCBombs;

    /**
     * 原始解析JSON数据
     */
    @TableField("raw_json")
    private String rawJson;

    /**
     * 游戏标题（非数据库字段，JOIN查询时填充）
     */
    @TableField(exist = false)
    private String gameTitle;

    /**
     * 各关卡得分列表（非数据库字段，从JSON解析而来）
     */
    @TableField(exist = false)
    private List<Long> stageScoresList;

    /**
     * 各关卡炸弹统计列表（非数据库字段，从JSON解析而来）
     */
    @TableField(exist = false)
    private List<StageBombStats> bombStatsList;

    /**
     * 解析错误列表（非数据库字段，用于记录解析过程中的错误）
     */
    @TableField(exist = false)
    private List<String> errors = new ArrayList<>();

    /**
     * 无参构造函数
     */
    public Replay() {
    }

    /**
     * 添加解析错误信息
     * 
     * @param error 错误信息字符串
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * 检查是否有解析错误
     * 
     * @return 如果有错误返回true，否则返回false
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 获取错误列表的副本
     * 
     * @return 错误信息列表
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * 获取完整的自机类型描述（角色 + 自机类型）
     * 
     * @return 完整的自机类型字符串，例如 "灵梦 封印"
     */
    public String getFullShotType() {
        if (character == null)
            return "Unknown";
        if (shotType == null || shotType.isEmpty())
            return character;
        return character + " " + shotType;
    }

    /**
     * 获取格式化的总分（带千位分隔符）
     * 
     * @return 格式化的分数字符串，例如 "1,234,567,890"
     */
    public String getFormattedScore() {
        return String.format("%,d", totalScore);
    }

    /**
     * 获取难度的简短显示名
     * 
     * @return 难度缩写，例如 E/N/H/L/Ex/Ph
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
     * 获取到达的关卡编号
     * 
     * 特殊关卡：Extra返回7，Phantasm返回8，通关返回99
     * 
     * @return 关卡编号，无法识别返回0
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
     * 获取指定关卡的得分
     * 
     * @param stageNum 关卡编号（从1开始）
     * @return 该关卡的得分，如果不存在返回0
     */
    public long getStageScore(int stageNum) {
        if (stageScoresList == null || stageNum < 1 || stageNum > stageScoresList.size()) {
            return 0;
        }
        return stageScoresList.get(stageNum - 1);
    }

    /**
     * 获取指定关卡的炸弹使用数量
     * 
     * @param stageNum 关卡编号（从1开始）
     * @return 该关卡的炸弹使用数量，如果不存在返回0
     */
    public int getStageBombCount(int stageNum) {
        if (bombStatsList == null || stageNum < 1 || stageNum > bombStatsList.size()) {
            return 0;
        }
        return bombStatsList.get(stageNum - 1).xCount;
    }

    /**
     * 设置关卡得分JSON并解析为列表
     * 
     * @param stageScoresJson JSON格式的关卡得分数据
     */
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

    /**
     * 获取Replay的字符串表示
     * 
     * @return 包含游戏标题、自机、难度、得分和通关标记的字符串
     */
    @Override
    public String toString() {
        return String.format("%s %s %s %s %s",
                gameTitle != null ? gameTitle : "Unknown",
                getFullShotType(),
                getDifficultyDisplay(),
                getFormattedScore(),
                cleared ? "✓" : "");
    }

    /**
     * 单关卡炸弹使用统计类
     * 
     * 记录单个关卡的炸弹使用情况，包括Z键和X键炸弹的数量，
     * 以及开始和结束的帧数。
     */
    public static class StageBombStats {
        public int stageNum;
        public int zCount;
        public int xCount;
        public int startFrame;
        public int endFrame;
    }
}
