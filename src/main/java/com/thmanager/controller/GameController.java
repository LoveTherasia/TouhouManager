package com.thmanager.controller;

import com.thmanager.model.Game;
import com.thmanager.dao.GameDAO;
import com.thmanager.service.GameLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

//游戏信息控制类
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameDAO gameDAO;
    private final GameLauncher gameLauncher;

    @Autowired
    public GameController(GameDAO gameDAO, GameLauncher gameLauncher) {
        this.gameDAO = gameDAO;
        this.gameLauncher = gameLauncher;
    }

    @GetMapping
    public List<Game> getGames() {
        return gameDAO.findAll();
    }

    @GetMapping("/{id}")
    public Game getGame(@PathVariable int id) {
        Optional<Game> game = gameDAO.findById(id);
        return game.orElse(null);
    }

    @PutMapping("/{id}/path")
    public Game updateGamePath(@PathVariable int id, @RequestBody String path) {
        Optional<Game> optionalGame = gameDAO.findById(id);
        if (optionalGame.isPresent()) {
            Game game = optionalGame.get();
            game.setInstallPath(path);
            game.setInstalled(true);
            gameDAO.update(game);
            return game;
        }
        return null;
    }

    @PostMapping("/{id}/launch")
    public void launchGame(@PathVariable int id, @RequestBody Map<String, Integer> request) {
        int countdown = request.getOrDefault("countdown", 3);
        Optional<Game> optionalGame = gameDAO.findById(id);
        if (optionalGame.isPresent()) {
            Game game = optionalGame.get();
            gameLauncher.launchWithCountdown(game, countdown, new GameLauncher.CountdownCallback() {
                @Override
                public void onTick(int seconds) {
                    // 可以通过WebSocket发送倒计时信息
                }

                @Override
                public void onFinish() {
                    // 倒计时完成
                }
            });
        }
    }

    @PostMapping("/force-stop")
    public boolean forceStopGame() {
        return gameLauncher.forceStop();
    }

    @GetMapping("/status")
    public Map<String, Object> getGameStatus() {
        return Map.of(
            "running", gameLauncher.isGameRunning(),
            "currentGame", gameLauncher.getCurrentGame().orElse(null)
        );
    }

    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable int id) {
        gameDAO.Delete(id);
    }
}