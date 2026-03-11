package com.thmanager.service;

import com.thmanager.dao.GameDAO;
import com.thmanager.model.Game;
import com.thmanager.model.Replay;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Replay扫描服务类
 * 
 * 提供Replay文件的扫描和播放功能：
 * - 扫描所有已安装游戏的Replay文件夹
 * - 扫描指定游戏的Replay文件
 * - 使用游戏程序播放Replay
 * 
 * @Service 注解表示这是一个服务层组件。
 */
@Service
public class ReplayScanner {

    /**
     * 游戏数据访问对象
     */
    private final GameDAO gameDAO;

    /**
     * Replay解析器
     */
    private final ReplayParser parser;

    /**
     * 构造函数，依赖注入GameDAO
     * 
     * @param gameDAO 游戏数据访问对象
     */
    public ReplayScanner(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
        this.parser = new ReplayParser();
    }

    /**
     * 扫描所有已安装游戏的Replay文件夹
     * 
     * 遍历所有已安装的游戏，扫描它们的Replay文件夹，
     * 解析发现的 .rpy 文件并返回导入的总数
     * 
     * @return 成功导入的 Replay 数量
     */
    public int scanAllGames() {
        int importedCount = 0;
        List<Game> installedGames = gameDAO.findInstalled();

        for (Game game : installedGames) {
            if (game.getReplayFolder() != null) {
                List<Replay> replays = scanGameReplays(game);
                importedCount += replays.size();
            }
        }

        return importedCount;
    }

    /**
     * 扫描指定游戏的Replay文件夹
     * 
     * 查找游戏Replay文件夹中的所有 .rpy 文件，
     * 使用ReplayParser解析每个文件并返回Replay对象列表
     * 
     * @param game 要扫描的游戏对象
     * @return 解析成功的Replay列表
     */
    public List<Replay> scanGameReplays(Game game) {
        List<Replay> replays = new ArrayList<>();

        try {
            Path replayDir = Paths.get(game.getReplayFolder());

            System.out.println("Replay folder: " + replayDir);
            if (!Files.exists(replayDir) || !Files.isDirectory(replayDir)) {
                return replays;
            }

            try (Stream<Path> paths = Files.list(replayDir)) {
                List<Path> rpyFiles = paths
                        .filter(p -> p.toString().toLowerCase().endsWith(".rpy"))
                        .collect(Collectors.toList());

                for (Path rpy : rpyFiles) {
                    System.out.println("Replay file: " + rpy);
                    Optional<Replay> replay = parser.parse(rpy, game.getId());
                    if (replay.isPresent()) {
                        Replay r = replay.get();
                        r.setGameId(game.getId());
                        r.setGameTitle(game.getDisplayName());

                        System.out.println("successful scan" + r.getGameTitle());
                        replays.add(r);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("扫描replay失败 [" + game.getDisplayName() + "]: " + e.getMessage());
        }

        return replays;
    }

    /**
     * 使用游戏程序播放指定的Replay
     * 
     * 启动游戏可执行文件并传入Replay文件作为参数，
     * 让游戏直接播放该Replay
     * 
     * @param replay 要播放的Replay对象
     * @return 是否成功启动播放
     */
    public boolean playReplay(Replay replay) {
        Optional<Game> game = gameDAO.findById(replay.getGameId());
        if (game.isEmpty())
            return false;

        String exePath = game.get().getFullExePath();
        if (exePath == null)
            return false;

        try {
            Path workingDir = Paths.get(game.get().getInstallPath());
            String replayArg = "replay\\" + replay.getFileName();

            ProcessBuilder pb = new ProcessBuilder(exePath, replayArg);
            pb.directory(workingDir.toFile());
            pb.start();

            return true;
        } catch (IOException e) {
            System.err.println("播放replay失败: " + e.getMessage());
            return false;
        }
    }
}