package com.thmanager.service;

import com.thmanager.dao.GameDAO;
import com.thmanager.dao.PlaySessionDAO;
import com.thmanager.model.Game;
import com.thmanager.model.PlaySession;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GameLauncher {
    private final GameDAO gameDAO;
    private final PlaySessionDAO sessionDAO;
    private final ExecutorService executor;
    private Process currentProcess;
    private PlaySession currentSession;
    private Game currentGame;
    private Runnable onGameStart;
    private Runnable onGameEnd;

    public GameLauncher(GameDAO gameDAO, PlaySessionDAO sessionDAO) {
        this.gameDAO = gameDAO;
        this.sessionDAO = sessionDAO;
        this.executor = Executors.newSingleThreadExecutor();
    }

    // 设置回调
    public void setOnGameStart(Runnable callback) {
        this.onGameStart = callback;
    }

    public void setOnGameEnd(Runnable callback) {
        this.onGameEnd = callback;
    }

    public void launchWithCountdown(Game game, int seconds, CountdownCallback callback) {
        System.out.println("==========================================");
        System.out.println("[GameLauncher] launchWithCountdown被调用");
        System.out.println("[GameLauncher] 游戏: " + (game != null ? game.getDisplayName() : "null"));
        System.out.println("[GameLauncher] 倒计时: " + seconds + "秒");
        System.out.println("==========================================");

        executor.submit(() -> {
            System.out.println("[GameLauncher] 倒计时线程开始");
            try {
                for (int i = seconds; i > 0; i--) {
                    System.out.println("[GameLauncher] 倒计时: " + i);
                    final int count = i;
                    callback.onTick(count);
                    Thread.sleep(1000);
                }

                System.out.println("[GameLauncher] 倒计时完成，准备启动游戏");
                callback.onFinish();
                launchGame(game);
            } catch (InterruptedException e) {
                System.err.println("[GameLauncher] 倒计时被中断");
                Thread.currentThread().interrupt();
            }
        });
    }

    // 启动游戏
    public void launchGame(Game game) {
        System.out.println("==========================================");
        System.out.println("[GameLauncher] launchGame被调用");
        System.out.println("==========================================");

        if (currentProcess != null && currentProcess.isAlive()) {
            System.err.println("[GameLauncher] 已有游戏在运行中！");
            throw new IllegalArgumentException("已有游戏在运行中");
        }

        String exePath = game.getFullExePath();
        System.out.println("[GameLauncher] EXE路径: " + exePath);
        if (exePath == null || !new File(exePath).exists()) {
            System.err.println("[GameLauncher] 游戏可执行文件不存在: " + exePath);
            throw new RuntimeException("游戏可执行文件不存在: " + exePath);
        }
        System.out.println("[GameLauncher] EXE文件存在");

        try {
            Path WorkingDir = Paths.get(game.getInstallPath());
            System.out.println("[GameLauncher] 工作目录: " + WorkingDir);

            ProcessBuilder pb = new ProcessBuilder(exePath);
            pb.directory(WorkingDir.toFile());
            pb.inheritIO();// 继承IO
            System.out.println("[GameLauncher] ProcessBuilder创建完成");

            System.out.println("[GameLauncher] 正在启动进程...");
            currentProcess = pb.start();
            System.out.println("[GameLauncher] 进程启动成功！进程ID: " + currentProcess.pid());
            currentGame = game;

            // 创建游玩会话
            System.out.println("[GameLauncher] 创建游玩会话");
            currentSession = new PlaySession(game.getId(), LocalDateTime.now());
            sessionDAO.create(currentSession);
            System.out.println("[GameLauncher] 游玩会话创建成功，ID: " + currentSession.getId());

            // 通知游戏开始
            if (onGameStart != null) {
                System.out.println("[GameLauncher] 调用onGameStart回调");
                onGameStart.run();
            }

            // 启动监控线程
            System.out.println("[GameLauncher] 启动监控线程");
            startMonitoring();
            System.out.println("[GameLauncher] launchGame完成");

        } catch (IOException e) {
            System.err.println("[GameLauncher] 启动游戏失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("fail to launch game: " + e.getMessage(), e);
        }
    }

    // 监控游戏进程
    private void startMonitoring() {
        System.out.println("[GameLauncher] 监控线程已启动");
        executor.submit(() -> {
            try {
                System.out.println("[GameLauncher] 等待游戏进程结束...");
                // 等待进程结束
                int exitCode = currentProcess.waitFor();
                System.out.println("[GameLauncher] 游戏进程已结束，退出码: " + exitCode);

                // 游戏结束，更新记录
                endCurrentSession();
                System.out.println("[GameLauncher] The game is over");
            } catch (InterruptedException e) {
                System.err.println("[GameLauncher] 监控线程被中断");
                Thread.currentThread().interrupt();
            }
        });
    }

    // 结束会话
    private void endCurrentSession() {
        System.out.println("[GameLauncher] endCurrentSession被调用");
        if (currentSession == null || currentGame == null) {
            System.err.println("[GameLauncher] currentSession或currentGame为null，无法结束会话");
            return;
        }

        // 计算时长
        System.out.println("[GameLauncher] 计算会话时长");
        currentSession.endSession();

        // 更新数据库
        System.out.println("[GameLauncher] 更新数据库");
        sessionDAO.endSession(
                currentSession.getId(),
                currentSession.getEndTime(),
                currentSession.getDurationSeconds());
        System.out.println("[GameLauncher] 数据库更新完成");

        // 更新游戏总时长
        long totalTime = sessionDAO.getToTotalPlayTimeByGame(currentGame.getId());
        System.out.println("[GameLauncher] Total play time: " + totalTime);
        currentGame.setTotalPlayTimeSeconds(totalTime);
        currentGame.setLastPlayed(LocalDateTime.now());
        currentGame.setInstalled(true);
        gameDAO.update(currentGame);
        System.out.println("[GameLauncher] 游戏信息更新完成");

        // 清理
        final Game endedGame = currentGame;
        currentGame = null;
        currentSession = null;
        currentProcess = null;
        System.out.println("[GameLauncher] 清理完成");

        // 更新UI
        if (onGameEnd != null) {
            System.out.println("[GameLauncher] 调用onGameEnd回调");
            onGameEnd.run();
        }
    }

    // 强制结束游戏
    public boolean forceStop() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
            endCurrentSession();
            return true;
        }
        return false;
    }

    // 检查是否有游戏正在运行
    public boolean isGameRunning() {
        return currentProcess != null && currentProcess.isAlive();
    }

    // 获取当前运行的游戏
    public Optional<Game> getCurrentGame() {
        return Optional.ofNullable(currentGame);
    }

    // 获取当前会话已运行事件
    public long getCurrentSessionDuration() {
        if (currentSession == null)
            return 0;
        return java.time.Duration.between(currentSession.getStartTime(), LocalDateTime.now()).getSeconds();
    }

    public void shutdown() {
        executor.shutdown();
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
        }
    }

    public interface CountdownCallback {
        void onTick(int seconds);

        void onFinish();
    }
}
