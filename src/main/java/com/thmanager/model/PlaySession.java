package com.thmanager.model;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

//游戏会话实体类
@Data
public class PlaySession {
    private int id;
    private int gameId;// 关联的游戏ID
    private LocalDateTime startTime;// 游戏开始时间
    private LocalDateTime endTime;// 游戏结束时间
    private long durationSeconds;// 持续秒数
    private String sessionType;// 会话类型
    private String note;// 备注

    public enum SessionType {
        NORMAL("normal", "正常游玩"),
        PRACTICE("practice", "练习模式"),
        REPLAY("replay", "观看回放");

        private final String code;
        private final String displayName;

        SessionType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() {
            return code;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public PlaySession() {
    }

    public PlaySession(int gameId, LocalDateTime startTime) {
        this.gameId = gameId;
        this.startTime = startTime;
        this.sessionType = SessionType.NORMAL.getCode();
    }

    // 结束会话时计算时长
    public void endSession() {
        this.endTime = LocalDateTime.now();
        this.durationSeconds = Duration.between(this.startTime, this.endTime).toSeconds();
    }

    // 格式化时长显示
    public String getFormattedDuration() {
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}
