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

//统计信息获取控制类
@RestController
@RequestMapping("api/statistics")
public class StatisticsController {
    private final ReplayDAO replayDAO;
    private final GameDAO gameDAO;
    private final ReplayStatisticsService statisticsService;

    @Autowired
    public StatisticsController(ReplayDAO replayDAO,GameDAO gameDAO,ReplayStatisticsService statisticsService){
        this.replayDAO=replayDAO;
        this.gameDAO=gameDAO;
        this.statisticsService=statisticsService;
    }

    //获取统计数据
    @GetMapping
    public Map<String,Object> getStatistics(){
        List<Replay> allReplays = replayDAO.findAll();
        List<Game> allGames = gameDAO.findAll();

        long totalGames = allGames.size();
        long totalReplays = allReplays.size();
        long clearedCount = allReplays.stream().filter(Replay::isCleared).count();

        long totalFrames = allReplays.stream().mapToLong(Replay::getTotalFrames).sum();
        long totalPlayTimeMinutes = totalFrames / 60 / 60;

        Map<String,Object> stats = new HashMap<>();
        stats.put("totalGames",totalGames);
        stats.put("totalReplays",totalReplays);
        stats.put("clearedCount",clearedCount);
        stats.put("totalPlayTime",totalPlayTimeMinutes);
        stats.put("ClearRate",totalReplays > 0 ? (double)clearedCount / totalReplays * 100 : 0);


        return stats;
    }

    //获取特定游戏的统计数据
    @GetMapping("/game/{gameId}")
    public Map<String,Object> getGameStatistics(@PathVariable int gameId){
        List<Replay> replays = replayDAO.findByGame(gameId);
        Map<String,Object> stats = new HashMap<>();

        stats.put("totalReplays",replays.size());
        stats.put("clearedCount",replays.stream().filter(Replay::isCleared).count());
        stats.put("bestScores",statisticsService.getBestScoresByDifficulty(gameId));

        return stats;
    }

    //获取所有游戏游玩时间
    @GetMapping("/playtime")
    public List<Map<String,Object>> getPlayTimeStats(){
        List<Replay> allReplays = replayDAO.findAll();
        Map<Integer,List<Replay>> byGame = allReplays.stream().collect(Collectors.groupingBy(Replay::getGameId));
        List<Map<String,Object>> result = new ArrayList<>();

        for(Map.Entry<Integer,List<Replay>> entry : byGame.entrySet()){
            int gameId = entry.getKey();
            List<Replay> gameReplays = entry.getValue();

            long totalFrames = gameReplays.stream().mapToLong(Replay::getTotalFrames).sum();
            long playTimeMinutes = totalFrames / 60 / 60;

            Game game = gameDAO.findById(gameId).orElse(null);

            Map<String,Object> item = new HashMap<>();
            item.put("gameId",gameId);
            item.put("gameName",game != null ? game.getDisplayName() : "未知游戏");
            item.put("playTime",playTimeMinutes);
            item.put("replayCount",gameReplays.size());

            result.add(item);
        }

        return result.stream().sorted((a,b) -> Long.compare((Long)b.get("playTime"),(Long)a.get("playTime"))).collect(Collectors.toList());
    }

    @GetMapping("/scores")
    public List<Map<String,Object>> getScoreStats(){
        List<Replay> allReplays = replayDAO.findAll();

        return allReplays.stream().sorted(
                (a,b) -> Long.compare(b.getTotalScore(),a.getTotalScore())
        ).limit(20).map(replay -> {
            Map<String,Object> item = new HashMap<>();
            item.put("id",replay.getId());
            item.put("gameTitle",replay.getGameTitle());
            item.put("PlayerName",replay.getPlayerName());
            item.put("difficulty",replay.getDifficulty());
            item.put("totalScore",replay.getTotalScore());
            item.put("date",replay.getGameDate() != null ? replay.getGameDate().toString() : null);

            return item;
        }).collect(Collectors.toList());
    }

    @GetMapping("/difficulty/{gameId}/{difficulty}")
    public ReplayStatisticsService.DifficultyFullReport getDifficultyReport(
            @PathVariable int gameId,
            @PathVariable String difficulty
    ){
        return statisticsService.generateDifficultyReport(gameId,difficulty);
    }
}
