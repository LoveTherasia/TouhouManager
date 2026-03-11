package com.thmanager.controller;

import com.thmanager.model.Game;
import com.thmanager.dao.GameDAO;
import com.thmanager.service.GameLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 游戏信息REST API控制器
 * 
 * 提供游戏管理相关的REST接口，包括：
 * - 获取游戏列表和详情
 * - 更新游戏安装路径
 * - 启动和停止游戏
 * - 查询游戏运行状态
 * 
 * 所有接口都映射在 /api/games 路径下。
 */
@RestController
@RequestMapping(value = "/api/games",method = RequestMethod.PUT)
public class GameController {

    /**
     * 游戏数据访问对象
     */
    private final GameDAO gameDAO;

    /**
     * 游戏启动器服务
     */
    private final GameLauncher gameLauncher;

    /**
     * 构造函数，依赖注入
     * 
     * @param gameDAO      游戏数据访问对象
     * @param gameLauncher 游戏启动器服务
     */
    @Autowired
    public GameController(GameDAO gameDAO, GameLauncher gameLauncher) {
        this.gameDAO = gameDAO;
        this.gameLauncher = gameLauncher;
    }

    /**
     * 获取所有游戏列表
     * 
     * GET /api/games
     * 
     * @return 所有游戏的列表
     */
    @GetMapping
    public List<Game> getGames() {
        return gameDAO.findAll();
    }

    /**
     * 根据ID获取单个游戏详情
     * 
     * GET /api/games/{id}
     * 
     * @param id 游戏ID
     * @return 游戏对象，如果不存在则返回null
     */
    @GetMapping("/{id}")
    public Game getGame(@PathVariable int id) {
        Optional<Game> game = gameDAO.findById(id);
        return game.orElse(null);
    }

    /**
     * 更新游戏安装路径
     * 
     * PUT /api/games/{id}/path
     * 
     * @param id   游戏ID
     * @param path 新的安装路径
     * @return 更新后的游戏对象，如果游戏不存在则返回null
     */
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

    /**
     * 启动指定游戏（带倒计时）
     * 
     * POST /api/games/{id}/launch
     * 
     * @param id      游戏ID
     * @param request 包含倒计时秒数的请求体，默认为3秒
     */
    @PostMapping("/{id}/launch")
    public void launchGame(@PathVariable int id, @RequestBody Map<String, Integer> request) {
        int countdown = request.getOrDefault("countdown", 3);
        Optional<Game> optionalGame = gameDAO.findById(id);
        if (optionalGame.isPresent()) {
            Game game = optionalGame.get();
            gameLauncher.launchWithCountdown(game, countdown, new GameLauncher.CountdownCallback() {
                @Override
                public void onTick(int seconds) {
                }

                @Override
                public void onFinish() {
                }
            });
        }
    }

    /**
     * 强制停止当前运行的游戏
     * 
     * POST /api/games/force-stop
     * 
     * @return 是否成功停止游戏
     */
    @PostMapping("/force-stop")
    public boolean forceStopGame() {
        return gameLauncher.forceStop();
    }

    /**
     * 获取游戏运行状态
     * 
     * GET /api/games/status
     * 
     * @return 包含运行状态和当前游戏信息的Map
     */
    @GetMapping("/status")
    public Map<String, Object> getGameStatus() {
        return Map.of(
                "running", gameLauncher.isGameRunning(),
                "currentGame", gameLauncher.getCurrentGame().orElse(null));
    }

    /**
     * 删除指定游戏记录
     * 
     * DELETE /api/games/{id}
     * 
     * @param id 游戏ID
     */
    @DeleteMapping("/{id}")
    public void deleteGame(@PathVariable int id) {
        gameDAO.Delete(id);
    }
}