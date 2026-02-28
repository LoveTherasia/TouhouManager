package com.thmanager.service;

import com.thmanager.dao.GameDAO;
import com.thmanager.model.Game;
import com.thmanager.model.Replay;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReplayScanner {

    private final GameDAO gameDAO;
    private final ReplayParser parser;

    public ReplayScanner() {
        this.gameDAO = new GameDAO();
        this.parser = new ReplayParser();
    }

    /**
     * 扫描所有已安装游戏的replay文件夹
     */
    public List<Replay> scanAllGames() {
        List<Replay> allReplays = new ArrayList<>();
        List<Game> installedGames = gameDAO.findInstalled();

        for (Game game : installedGames) {
            if (game.getReplayFolder() != null) {
                List<Replay> replays = scanGameReplays(game);
                allReplays.addAll(replays);
            }
        }

        // 按日期降序排序
        allReplays.sort((a, b) -> {
            if (a.getDate() == null || b.getDate() == null) return 0;
            return b.getDate().compareTo(a.getDate());
        });

        return allReplays;
    }

    /**
     * 扫描指定游戏的replay
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
                    Optional<Replay> replay = parser.parse(rpy);
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
     * 播放指定replay
     */
    public boolean playReplay(Replay replay) {
        Optional<Game> game = gameDAO.findById(replay.getGameId());
        if (game.isEmpty()) return false;

        String exePath = game.get().getFullExePath();
        if (exePath == null) return false;

        try {
            // 东方游戏启动replay的参数
            // 通常是 thxx.exe "replay\filename.rpy"
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