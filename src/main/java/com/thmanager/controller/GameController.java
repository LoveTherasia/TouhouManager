package com.thmanager.controller;

import com.thmanager.model.Game;
import com.thmanager.dao.GameDAO;
import com.thmanager.service.GameLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
@RequestMapping("/api/games")
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
     * @param id      游戏ID
     * @param request 包含路径的请求体
     * @return 更新后的游戏对象，如果游戏不存在则返回null
     */
    @PutMapping("/{id}/path")
    public Game updateGamePath(@PathVariable int id, @RequestBody Map<String, Object> request) {
        Optional<Game> optionalGame = gameDAO.findById(id);
        if (optionalGame.isPresent()) {
            Game game = optionalGame.get();
            // 处理不同格式的路径数据
            String path = null;
            Object pathObj = request.get("path");
            if (pathObj instanceof String) {
                path = (String) pathObj;
            } else if (pathObj instanceof Map) {
                // 处理嵌套的path对象
                Map<?, ?> nestedMap = (Map<?, ?>) pathObj;
                Object nestedPath = nestedMap.get("path");
                if (nestedPath instanceof String) {
                    path = (String) nestedPath;
                }
            }
            // 清理路径
            if (path != null) {
                path = path.trim();
                // 移除可能的引号
                if (path.startsWith("\"") && path.endsWith("\"")) {
                    path = path.substring(1, path.length() - 1);
                }
            }
            game.setInstallPath(path);
            game.setInstalled(path != null && !path.isEmpty());
            gameDAO.update(game);
            return game;
        }
        return null;
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
        List<Game> games = gameDAO.findAll();
        for (Game game : games) {
            String installPath = game.getInstallPath();
            game.setInstalled(installPath != null && !installPath.trim().isEmpty());
        }
        return games;
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
        Map<String, Object> status = new HashMap<>();
        status.put("running", gameLauncher.isGameRunning());
        status.put("currentGame", gameLauncher.getCurrentGame().orElse(null));
        return status;
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

    /**
     * 清空所有游戏的安装路径
     * 
     * POST /api/games/clear-paths
     * 
     * @return 清空成功的游戏数量
     */
    @PostMapping("/clear-paths")
    public int clearAllGamePaths() {
        List<Game> games = gameDAO.findAll();
        int clearedCount = 0;
        for (Game game : games) {
            game.setInstallPath(null);
            game.setInstalled(false);
            if (gameDAO.update(game)) {
                clearedCount++;
            }
        }
        return clearedCount;
    }
}