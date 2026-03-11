package com.thmanager.controller;

import com.thmanager.dao.GameDAO;
import com.thmanager.dao.ReplayDAO;
import com.thmanager.model.Game;
import com.thmanager.model.Replay;
import com.thmanager.service.ReplayStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 统计信息REST API控制器
 * 
 * 提供游戏数据统计相关的REST接口，包括：
 * - 总体统计概览
 * - 特定游戏的统计数据
 * - 各游戏游玩时间排行
 * - 最高分排行
 * - 特定难度的详细统计报告
 * 
 * 所有接口都映射在 /api/statistics 路径下。
 */
@RestController
@RequestMapping("api/statistics")
public class StatisticsController {

    /**
     * Replay数据访问对象
     */
    private final ReplayDAO replayDAO;

    /**
     * 游戏数据访问对象
     */
    private final GameDAO gameDAO;

    /**
     * Replay统计服务
     */
    private final ReplayStatisticsService statisticsService;

    /**
     * 构造函数，依赖注入
     * 
     * @param replayDAO         Replay数据访问对象
     * @param gameDAO           游戏数据访问对象
     * @param statisticsService Replay统计服务
     */
    @Autowired
    public StatisticsController(ReplayDAO replayDAO, GameDAO gameDAO, ReplayStatisticsService statisticsService) {
        this.replayDAO = replayDAO;
        this.gameDAO = gameDAO;
        this.statisticsService = statisticsService;
    }

    /**
     * 获取总体统计数据
     * 
     * GET /api/statistics
     * 
     * @return 包含游戏总数、Replay总数、通关次数、总游玩时长和通关率的统计Map
     */
    @GetMapping
    public Map<String, Object> getStatistics() {
        List<Replay> allReplays = replayDAO.findAll();
        List<Game> allGames = gameDAO.findAll();

        long totalGames = allGames.size();
        long totalReplays = allReplays.size();
        long clearedCount = allReplays.stream().filter(Replay::isCleared).count();

        long totalFrames = allReplays.stream().mapToLong(Replay::getTotalFrames).sum();
        long totalPlayTimeMinutes = totalFrames / 60 / 60;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGames", totalGames);
        stats.put("totalReplays", totalReplays);
        stats.put("clearedCount", clearedCount);
        stats.put("totalPlayTime", totalPlayTimeMinutes);
        stats.put("ClearRate", totalReplays > 0 ? (double) clearedCount / totalReplays * 100 : 0);

        return stats;
    }

    /**
     * 获取特定游戏的统计数据
     * 
     * GET /api/statistics/game/{gameId}
     * 
     * @param gameId 游戏ID
     * @return 包含该游戏Replay总数、通关次数和各难度最高分的统计Map
     */
    @GetMapping("/game/{gameId}")
    public Map<String, Object> getGameStatistics(@PathVariable int gameId) {
        List<Replay> replays = replayDAO.findByGame(gameId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalReplays", replays.size());
        stats.put("clearedCount", replays.stream().filter(Replay::isCleared).count());
        stats.put("bestScores", statisticsService.getBestScoresByDifficulty(gameId));

        return stats;
    }

    /**
     * 获取所有游戏游玩时间排行
     * 
     * GET /api/statistics/playtime
     * 
     * @return 按游玩时间降序排列的游戏列表，每项包含游戏ID、名称、游玩时间和Replay数量
     */
    @GetMapping("/playtime")
    public List<Map<String, Object>> getPlayTimeStats() {
        List<Replay> allReplays = replayDAO.findAll();
        Map<Integer, List<Replay>> byGame = allReplays.stream().collect(Collectors.groupingBy(Replay::getGameId));
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<Integer, List<Replay>> entry : byGame.entrySet()) {
            int gameId = entry.getKey();
            List<Replay> gameReplays = entry.getValue();

            long totalFrames = gameReplays.stream().mapToLong(Replay::getTotalFrames).sum();
            long playTimeMinutes = totalFrames / 60 / 60;

            Game game = gameDAO.findById(gameId).orElse(null);

            Map<String, Object> item = new HashMap<>();
            item.put("gameId", gameId);
            item.put("gameName", game != null ? game.getDisplayName() : "未知游戏");
            item.put("playTime", playTimeMinutes);
            item.put("replayCount", gameReplays.size());

            result.add(item);
        }

        return result.stream()
                .sorted((a, b) -> Long.compare((Long) b.get("playTime"), (Long) a.get("playTime")))
                .collect(Collectors.toList());
    }

    /**
     * 获取最高分TOP 20排行
     * 
     * GET /api/statistics/scores
     * 
     * @return 按分数降序排列的前20条Replay记录
     */
    @GetMapping("/scores")
    public List<Map<String, Object>> getScoreStats() {
        List<Replay> allReplays = replayDAO.findAll();

        return allReplays.stream().sorted(
                (a, b) -> Long.compare(b.getTotalScore(), a.getTotalScore())).limit(20).map(replay -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", replay.getId());
                    item.put("gameTitle", replay.getGameTitle());
                    item.put("PlayerName", replay.getPlayerName());
                    item.put("difficulty", replay.getDifficulty());
                    item.put("totalScore", replay.getTotalScore());
                    item.put("date", replay.getGameDate() != null ? replay.getGameDate().toString() : null);

                    return item;
                }).collect(Collectors.toList());
    }

    /**
     * 获取特定游戏特定难度的详细统计报告
     * 
     * GET /api/statistics/difficulty/{gameId}/{difficulty}
     * 
     * @param gameId     游戏ID
     * @param difficulty 难度
     * @return 包含最高分、自机使用统计、到达关卡统计、炸弹使用统计等的完整报告
     */
    @GetMapping("/difficulty/{gameId}/{difficulty}")
    public ReplayStatisticsService.DifficultyFullReport getDifficultyReport(
            @PathVariable int gameId,
            @PathVariable String difficulty) {
        return statisticsService.generateDifficultyReport(gameId, difficulty);
    }
}
