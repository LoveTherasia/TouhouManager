package com.thmanager.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@TableName("play_sessions")
public class PlaySession {
    @TableId(type = IdType.AUTO)
    private int id;
    @TableField("game_id")
    private int gameId;
    @TableField("start_time")
    private LocalDateTime startTime;
    @TableField("end_time")
    private LocalDateTime endTime;
    @TableField("duration_seconds")
    private long durationSeconds;
    @TableField("session_type")
    private String sessionType;
    @TableField("note")
    private String note;

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

    public void endSession() {
        this.endTime = LocalDateTime.now();
        this.durationSeconds = Duration.between(this.startTime, this.endTime).toSeconds();
    }

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
