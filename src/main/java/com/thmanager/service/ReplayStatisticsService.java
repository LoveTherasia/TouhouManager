package com.thmanager.service;

import com.thmanager.dao.GameDAO;
import com.thmanager.dao.ReplayDAO;
import com.thmanager.model.Game;
import com.thmanager.model.Replay;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Replay专项统计服务
 * 实现：最高分追踪、机体使用统计、到达面数统计、每面炸弹统计
 */
public class ReplayStatisticsService {

    private final ReplayDAO replayDAO;
    private final GameDAO gameDAO;

    public ReplayStatisticsService() {
        this.replayDAO = new ReplayDAO();
        this.gameDAO = new GameDAO();
    }

    // ========== 1. 某游戏某难度最大分数 ==========

    public Optional<Replay> getBestScore(int gameId, String difficulty) {
        return replayDAO.findBestByDifficulty(gameId, difficulty);
    }

    public Map<String, Replay> getBestScoresByDifficulty(int gameId) {
        Map<String, Replay> bests = new LinkedHashMap<>();
        String[] difficulties = {"Easy", "Normal", "Hard", "Lunatic", "Extra", "Phantasm"};

        for (String diff : difficulties) {
            replayDAO.findBestByDifficulty(gameId, diff).ifPresent(r -> bests.put(diff, r));
        }
        return bests;
    }

    public boolean isNewHighScore(int gameId, String difficulty, long newScore) {
        Optional<Replay> currentBest = getBestScore(gameId, difficulty);
        return currentBest.isEmpty() || newScore > currentBest.get().getTotalScore();
    }

    // ========== 2. 某游戏某难度机体使用统计 ==========

    public List<ShotTypeUsage> getShotTypeUsage(int gameId, String difficulty) {
        List<Replay> replays = replayDAO.findByGame(gameId).stream()
                .filter(r -> difficulty.equalsIgnoreCase(r.getDifficulty()))
                .toList();

        Map<String, ShotTypeUsage> usageMap = new HashMap<>();

        for (Replay r : replays) {
            String shot = r.getFullShotType();
            ShotTypeUsage usage = usageMap.get(shot);
            if (usage == null) {
                usage = new ShotTypeUsage(shot, 0, 0, 0, 0, new ArrayList<>());
                usageMap.put(shot, usage);
            }

            usage.useCount++;
            usage.totalScore += r.getTotalScore();
            usage.bestScore = Math.max(usage.bestScore, r.getTotalScore());
            usage.records.add(r);
        }

        List<ShotTypeUsage> result = new ArrayList<>(usageMap.values());
        for (ShotTypeUsage u : result) {
            u.averageScore = u.totalScore / u.useCount;
        }

        return result.stream()
                .sorted(Comparator.comparingInt((ShotTypeUsage u) -> u.useCount).reversed())
                .collect(Collectors.toList());
    }

    // ========== 3. 某游戏某难度到达面数统计 ==========

    public List<StageReachStat> getStageReachStats(int gameId, String difficulty) {
        List<Replay> replays = replayDAO.findByGame(gameId).stream()
                .filter(r -> difficulty.equalsIgnoreCase(r.getDifficulty()))
                .toList();

        Map<Integer, StageReachStat> stats = new TreeMap<>();

        for (Replay r : replays) {
            int stageNumber = r.getReachedStageNumber();  // 使用不同的变量名
            if (stageNumber <= 0) continue;

            StageReachStat stat = stats.get(stageNumber);
            if (stat == null) {
                stat = new StageReachStat(stageNumber, 0, 0, 0, 0.0, new ArrayList<>());
                stats.put(stageNumber, stat);
            }

            stat.reachCount++;
            if (r.isCleared()) {
                stat.clearCount++;
            }
            stat.totalScore += r.getTotalScore();
            stat.records.add(r);
        }

        for (StageReachStat s : stats.values()) {
            s.clearRate = s.reachCount > 0 ? (double) s.clearCount / s.reachCount * 100 : 0.0;
        }

        return new ArrayList<>(stats.values());
    }

    public double getClearRate(int gameId, String difficulty) {
        List<Replay> replays = replayDAO.findByGame(gameId).stream()
                .filter(r -> difficulty.equalsIgnoreCase(r.getDifficulty()))
                .toList();

        if (replays.isEmpty()) return 0.0;

        long clearCount = replays.stream().filter(Replay::isCleared).count();
        return (double) clearCount / replays.size() * 100;
    }

    // ========== 4. 某游戏某难度各面炸弹使用统计 ==========

    public List<StageBombStat> getStageBombStats(int gameId, String difficulty) {
        List<Replay> replays = replayDAO.findByGame(gameId).stream()
                .filter(r -> difficulty.equalsIgnoreCase(r.getDifficulty()))
                .filter(r -> r.getBombStatsList() != null)
                .toList();

        if (replays.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, List<Replay.StageBombStats>> byStage = new HashMap<>();

        for (Replay r : replays) {
            for (Replay.StageBombStats stats : r.getBombStatsList()) {
                byStage.computeIfAbsent(stats.stageNum, k -> new ArrayList<>()).add(stats);
            }
        }

        List<StageBombStat> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Replay.StageBombStats>> entry : byStage.entrySet()) {
            int stageNumber = entry.getKey();  // 使用不同的变量名
            List<Replay.StageBombStats> list = entry.getValue();

            double avgZBombs = list.stream().mapToInt(s -> s.zCount).average().orElse(0.0);
            double avgXBombs = list.stream().mapToInt(s -> s.xCount).average().orElse(0.0);
            int maxZBombs = list.stream().mapToInt(s -> s.zCount).max().orElse(0);
            int maxXBombs = list.stream().mapToInt(s -> s.xCount).max().orElse(0);

            result.add(new StageBombStat(
                    stageNumber,
                    list.size(),
                    avgZBombs,
                    avgXBombs,
                    maxZBombs,
                    maxXBombs
            ));
        }

        return result.stream()
                .sorted(Comparator.comparingInt(StageBombStat::getStageNumber))
                .collect(Collectors.toList());
    }

    public Map<String, List<StageBombStat>> getBombStatsByShotType(int gameId, String difficulty) {
        List<Replay> replays = replayDAO.findByGame(gameId).stream()
                .filter(r -> difficulty.equalsIgnoreCase(r.getDifficulty()))
                .filter(r -> r.getBombStatsList() != null)
                .toList();

        Map<String, List<Replay>> byShot = replays.stream()
                .collect(Collectors.groupingBy(Replay::getFullShotType));

        Map<String, List<StageBombStat>> result = new HashMap<>();

        for (Map.Entry<String, List<Replay>> entry : byShot.entrySet()) {
            String shot = entry.getKey();
            List<Replay> shotReplays = entry.getValue();

            Map<Integer, List<Replay.StageBombStats>> byStage = new HashMap<>();
            for (Replay r : shotReplays) {
                for (Replay.StageBombStats stats : r.getBombStatsList()) {
                    byStage.computeIfAbsent(stats.stageNum, k -> new ArrayList<>()).add(stats);
                }
            }

            List<StageBombStat> stageStats = new ArrayList<>();
            for (Map.Entry<Integer, List<Replay.StageBombStats>> se : byStage.entrySet()) {
                List<Replay.StageBombStats> list = se.getValue();
                stageStats.add(new StageBombStat(
                        se.getKey(),
                        list.size(),
                        list.stream().mapToInt(s -> s.zCount).average().orElse(0.0),
                        list.stream().mapToInt(s -> s.xCount).average().orElse(0.0),
                        0, 0
                ));
            }

            result.put(shot, stageStats.stream()
                    .sorted(Comparator.comparingInt(StageBombStat::getStageNumber))
                    .collect(Collectors.toList()));
        }

        return result;
    }

    // ========== 5. 综合报告 ==========

    public DifficultyFullReport generateDifficultyReport(int gameId, String difficulty) {
        Game game = gameDAO.findById(gameId).orElse(null);
        if (game == null) return null;

        long totalAttempts = replayDAO.findByGame(gameId).stream()
                .filter(r -> difficulty.equalsIgnoreCase(r.getDifficulty()))
                .count();

        return new DifficultyFullReport(
                game.getDisplayName(),
                difficulty,
                getBestScore(gameId, difficulty).orElse(null),
                getShotTypeUsage(gameId, difficulty),
                getStageReachStats(gameId, difficulty),
                getStageBombStats(gameId, difficulty),
                getClearRate(gameId, difficulty),
                totalAttempts
        );
    }

    // ========== 数据类（使用标准Java Bean命名规范） ==========

    public static class ShotTypeUsage {
        private String shotType;
        private int useCount;
        private long totalScore;
        private long bestScore;
        private long averageScore;
        private List<Replay> records;

        public ShotTypeUsage(String shotType, int useCount, long totalScore,
                             long bestScore, long averageScore, List<Replay> records) {
            this.shotType = shotType;
            this.useCount = useCount;
            this.totalScore = totalScore;
            this.bestScore = bestScore;
            this.averageScore = averageScore;
            this.records = records;
        }

        // Getters
        public String getShotType() { return shotType; }
        public int getUseCount() { return useCount; }
        public long getTotalScore() { return totalScore; }
        public long getBestScore() { return bestScore; }
        public long getAverageScore() { return averageScore; }
        public List<Replay> getRecords() { return records; }
    }

    public static class StageReachStat {
        private int stageNumber;      // 改名为 stageNumber 避免混淆
        private int reachCount;
        private int clearCount;
        private long totalScore;
        private double clearRate;
        private List<Replay> records;

        public StageReachStat(int stageNumber, int reachCount, int clearCount,
                              long totalScore, double clearRate, List<Replay> records) {
            this.stageNumber = stageNumber;
            this.reachCount = reachCount;
            this.clearCount = clearCount;
            this.totalScore = totalScore;
            this.clearRate = clearRate;
            this.records = records;
        }

        // Getters
        public int getStageNumber() { return stageNumber; }  // 明确命名
        public int getReachCount() { return reachCount; }
        public int getClearCount() { return clearCount; }
        public long getTotalScore() { return totalScore; }
        public double getClearRate() { return clearRate; }
        public List<Replay> getRecords() { return records; }
    }

    public static class StageBombStat {
        private int stageNumber;      // 改名为 stageNumber
        private int sampleCount;
        private double avgZBombs;
        private double avgXBombs;
        private int maxZBombs;
        private int maxXBombs;

        public StageBombStat(int stageNumber, int sampleCount, double avgZBombs,
                             double avgXBombs, int maxZBombs, int maxXBombs) {
            this.stageNumber = stageNumber;
            this.sampleCount = sampleCount;
            this.avgZBombs = avgZBombs;
            this.avgXBombs = avgXBombs;
            this.maxZBombs = maxZBombs;
            this.maxXBombs = maxXBombs;
        }

        // Getters
        public int getStageNumber() { return stageNumber; }  // 明确命名
        public int getSampleCount() { return sampleCount; }
        public double getAvgZBombs() { return avgZBombs; }
        public double getAvgXBombs() { return avgXBombs; }
        public int getMaxZBombs() { return maxZBombs; }
        public int getMaxXBombs() { return maxXBombs; }
    }

    public static class DifficultyFullReport {
        private String gameName;
        private String difficulty;
        private Replay bestRecord;
        private List<ShotTypeUsage> shotUsage;
        private List<StageReachStat> stageStats;
        private List<StageBombStat> bombStats;
        private double overallClearRate;
        private long totalAttempts;

        public DifficultyFullReport(String gameName, String difficulty, Replay bestRecord,
                                    List<ShotTypeUsage> shotUsage, List<StageReachStat> stageStats,
                                    List<StageBombStat> bombStats, double overallClearRate,
                                    long totalAttempts) {
            this.gameName = gameName;
            this.difficulty = difficulty;
            this.bestRecord = bestRecord;
            this.shotUsage = shotUsage;
            this.stageStats = stageStats;
            this.bombStats = bombStats;
            this.overallClearRate = overallClearRate;
            this.totalAttempts = totalAttempts;
        }

        // Getters
        public String getGameName() { return gameName; }
        public String getDifficulty() { return difficulty; }
        public Replay getBestRecord() { return bestRecord; }
        public List<ShotTypeUsage> getShotUsage() { return shotUsage; }
        public List<StageReachStat> getStageStats() { return stageStats; }
        public List<StageBombStat> getBombStats() { return bombStats; }
        public double getOverallClearRate() { return overallClearRate; }
        public long getTotalAttempts() { return totalAttempts; }
    }
}