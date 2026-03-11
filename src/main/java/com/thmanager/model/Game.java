package com.thmanager.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.time.LocalDateTime;

/**
 * 游戏数据模型类
 * 
 * 该类表示东方Project系列的一款游戏，包含游戏的基本信息、安装配置和游玩统计数据。
 * 对应数据库中的 games 表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("games")
public class Game {

    /**
     * 游戏主键ID，自增
     */
    @TableId(type = IdType.AUTO)
    private int id;

    /**
     * 游戏编号（整数作编号，例如 TH06 的编号为 6）
     */
    @TableField("game_number")
    private int gameNumber;

    /**
     * 游戏日文标题
     */
    @TableField("title_ja")
    private String titleJa;

    /**
     * 游戏中文标题
     */
    @TableField("title_zh")
    private String titleCn;

    /**
     * 游戏英文标题
     */
    @TableField("title_en")
    private String titleEn;

    /**
     * 游戏安装路径（完整的本地文件系统路径）
     */
    @TableField("install_path")
    private String installPath;

    /**
     * 游戏可执行文件名（例如 th06.exe）
     */
    @TableField("exe_name")
    private String exeName;

    /**
     * Replay文件存放文件夹名称
     */
    @TableField("replay_folder")
    private String replayFolder;

    /**
     * 游戏封面图片路径
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 总游玩时长（单位：秒）
     */
    @TableField("total_play_time_seconds")
    private long totalPlayTimeSeconds;

    /**
     * 最后游玩时间
     */
    @TableField("last_played")
    private LocalDateTime lastPlayed;

    /**
     * 游戏是否已安装
     */
    @TableField("is_installed")
    private boolean installed;

    /**
     * 游戏记录创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 获取格式化的显示名称
     * 
     * 格式为：THXX - 标题
     * 优先显示中文标题，如果中文标题不存在则显示日文标题
     * 
     * @return 格式化的游戏显示名称
     */
    public String getDisplayName() {
        return String.format("TH%02d - %s", gameNumber, titleCn != null ? titleCn : titleJa);
    }

    /**
     * 获取游戏短名称（用于图片文件名）
     * 
     * 格式为：thxx（小写）
     * 
     * @return 游戏短名称
     */
    public String getShortName() {
        return String.format("th%02d", gameNumber);
    }

    /**
     * 获取格式化的总游玩时间
     * 
     * 将秒数转换为易读的格式，例如："2小时30分钟" 或 "45分钟"
     * 
     * @return 格式化的游玩时间字符串
     */
    public String getFormattedPlayTime() {
        long hours = totalPlayTimeSeconds / 3600;
        long minutes = (totalPlayTimeSeconds % 3600) / 60;
        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }

    /**
     * 获取完整可执行文件路径
     * 
     * 优先查找带 "c" 后缀的可执行文件（通常是窗口化版本），
     * 如果带 "c" 的文件不存在，则返回原始可执行文件路径
     * 
     * @return 完整的可执行文件路径，如果配置不完整则返回 null
     */
    public String getFullExePath() {
        if (installPath == null || installPath.isEmpty() || exeName == null || exeName.isEmpty()) {
            return null;
        }

        String[] exeNameParts = exeName.split("\\.(?=[^.]+$)");
        if (exeNameParts.length != 2) {
            String originalPath = installPath + File.separator + exeName;
            File originalFile = new File(originalPath);
            if (originalFile.exists() && originalFile.isFile()) {
                return originalPath;
            }
            return null;
        }

        String exeNameWithC = exeNameParts[0] + "c." + exeNameParts[1];
        String exePathWithC = installPath + File.separator + exeNameWithC;

        File fileWithC = new File(exePathWithC);
        if (fileWithC.exists() && fileWithC.isFile()) {
            return exePathWithC;
        }

        String originalPath = installPath + File.separator + exeName;
        File originalFile = new File(originalPath);
        if (originalFile.exists() && originalFile.isFile()) {
            return originalPath;
        }

        return null;
    }
}
