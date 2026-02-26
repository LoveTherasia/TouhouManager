package com.thmanager.model;

import lombok.Data;

import java.time.LocalDateTime;

//游戏实体类
public class Game {
    private int id;
    private int gameNumber;//整数作编号
    private String titleJa;//日文名
    private String titleCn;//中文名
    private String titleEn;//英文缩写
    private String installPath;//安装路径
    private String exeName;//可执行文件名
    private String replayFolder;//replay文件夹
    private String coverImage;//封面图
    private long totalPlayTimeSeconds;//总游玩时间
    private LocalDateTime lastPlayed;//最后游玩时间
    private boolean installed;//是否已安装
    private LocalDateTime createdAt;

    public Game() {}

    public Game(int gameNumber,String titleJa,String titleCn,String titleEn,String exeName){
        this.gameNumber = gameNumber;
        this.titleJa = titleJa;
        this.titleCn = titleCn;
        this.titleEn = titleEn;
    }

    // 格式化显示名称
    public String getDisplayName(){
        return String.format("TH%02d - %s",gameNumber,titleCn != null ? titleCn : titleJa);
    }

    // 格式化显示总游玩时间
    public String getFormattedPlayTime(){
        long hours = totalPlayTimeSeconds / 3600;
        long minutes = (totalPlayTimeSeconds % 3600) / 60;
        if(hours > 0){
            return String.format("%d小时%d分钟",hours,minutes);
        }else{
            return String.format("%d分钟",minutes);
        }
    }

    // 获取完整可执行文件路径
    public String getFullExePath(){
        if(installPath == null || installPath.isEmpty()){
            return null;
        }else{
            return installPath + "\\" + exeName;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGameNumber() { return gameNumber; }
    public void setGameNumber(int gameNumber) { this.gameNumber = gameNumber; }

    public String getTitleJa() { return titleJa; }
    public void setTitleJa(String titleJa) { this.titleJa = titleJa; }

    public String getTitleCn() { return titleCn; }
    public void setTitleCn(String titleCn) { this.titleCn = titleCn; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getInstallPath() { return installPath; }
    public void setInstallPath(String installPath) { this.installPath = installPath; }

    public String getExeName() { return exeName; }
    public void setExeName(String exeName) { this.exeName = exeName; }

    public String getReplayFolder() { return replayFolder; }
    public void setReplayFolder(String replayFolder) { this.replayFolder = replayFolder; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public long getTotalPlayTimeSeconds() { return totalPlayTimeSeconds; }
    public void setTotalPlayTimeSeconds(long totalPlayTimeSeconds) { this.totalPlayTimeSeconds = totalPlayTimeSeconds; }

    public LocalDateTime getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(LocalDateTime lastPlayed) { this.lastPlayed = lastPlayed; }

    public boolean isInstalled() { return installed; }
    public void setInstalled(boolean installed) { this.installed = installed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
