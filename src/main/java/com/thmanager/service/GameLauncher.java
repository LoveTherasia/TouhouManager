package com.thmanager.service;

import com.thmanager.dao.GameDAO;
import com.thmanager.dao.PlaySessionDAO;
import com.thmanager.model.Game;
import com.thmanager.model.PlaySession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameLauncher {
    private final GameDAO gameDAO;
    private final PlaySessionDAO sessionDAO;
    private final ExecutorService executor;
    private Process currentProcess;
    private PlaySession currentSession;
    private Game currentGame;
    private Runnable onGameStart;
    private Runnable onGameEnd;

    public GameLauncher() {
        this.gameDAO = new GameDAO();
        this.sessionDAO = new PlaySessionDAO();
        this.executor = Executors.newSingleThreadExecutor();
    }

    //设置回调
    public void setOnGameStart(Runnable callback){
        this.onGameStart = callback;
    }

    public void setOnGameEnd(Runnable callback){
        this.onGameEnd = callback;
    }

    //启动游戏
    public void launchWithCountdown(Game game, int seconds, CountdownCallback callback){
        executor.submit(() -> {
            try{
                for(int i = seconds;i > 0;i--){
                    final int count = i;
                    javafx.application.Platform.runLater(() -> callback.onTick(count));
                    Thread.sleep(1000);
                }

                javafx.application.Platform.runLater(() -> {
                    callback.onFinish();
                    launchGame(game);
                });
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        });
    }

    //启动游戏
    public void launchGame(Game game){
        if(currentProcess != null && currentProcess.isAlive()){
            throw new IllegalArgumentException("已有游戏在运行中");
        }

        String exePath = game.getFullExePath();
        if(exePath == null || !new File(exePath).exists()){
            throw new RuntimeException("游戏可执行文件不存在" + exePath);
        }

        try{
            Path WorkingDir = Paths.get(game.getInstallPath());

            ProcessBuilder pb = new ProcessBuilder(exePath);
            pb.directory(WorkingDir.toFile());
            pb.inheritIO();//继承IO

            currentProcess = pb.start();
            currentGame = game;

            //创建游玩会话
            currentSession = new PlaySession(game.getId(), LocalDateTime.now());
            sessionDAO.create(currentSession);

            //通知游戏开始
            if(onGameStart != null){
                javafx.application.Platform.runLater(onGameStart);
            }

            //启动监控线程
            startMonitoring();

        }catch(IOException e){
            throw new RuntimeException("fail to launch game" + e.getMessage(),e);
        }
    }

    //监控游戏进程
    private void startMonitoring(){
        executor.submit(() -> {
            try{
                //等待进程结束
                currentProcess.waitFor();

                //游戏结束，更新记录
                endCurrentSession();
                System.out.println("The game is over");
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        });
    }

    //结束会话
    private void endCurrentSession(){
        if(currentSession == null || currentGame == null) return ;

        //计算时长
        currentSession.endSession();

        //更新数据库
        sessionDAO.endSession(
                currentSession.getId(),
                currentSession.getEndTime(),
                currentSession.getDurationSeconds()
        );

        //更新游戏总时长
        long totalTime = sessionDAO.getToTotalPlayTimeByGame(currentGame.getId());
        System.out.println("Total play time: " + totalTime);
        currentGame.setTotalPlayTimeSeconds(totalTime);
        currentGame.setLastPlayed(LocalDateTime.now());
        currentGame.setInstalled(true);
        gameDAO.update(currentGame);

        //清理
        final Game endedGame = currentGame;
        currentGame = null;
        currentSession = null;
        currentProcess = null;

        //更新UI
        if(onGameEnd != null){
            javafx.application.Platform.runLater(() -> {
                onGameEnd.run();
            });
        }
    }
    //强制结束游戏
    public boolean forceStop(){
        if(currentProcess != null && currentProcess.isAlive()){
            currentProcess.destroy();
            endCurrentSession();
            return true;
        }
        return false;
    }

    //检查是否有游戏正在运行
    public boolean isGameRunning(){
        return currentProcess != null && currentProcess.isAlive();
    }

    //获取当前运行的游戏
    public Optional<Game> getCurrentGame(){
        return Optional.ofNullable(currentGame);
    }

    //获取当前会话已运行事件
    public long getCurrentSessionDuration(){
        if(currentSession == null) return 0;
        return java.time.Duration.between(currentSession.getStartTime(), LocalDateTime.now()).getSeconds();
    }

    public void shutdown(){
        executor.shutdown();
        if(currentProcess != null && currentProcess.isAlive()){
            currentProcess.destroy();
        }
    }

    public interface CountdownCallback{
        void onTick(int seconds);
        void onFinish();
    }
}
