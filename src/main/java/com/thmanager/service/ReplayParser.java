package com.thmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thmanager.model.Replay;
import com.thmanager.model.Replay.StageBombStats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ReplayParser {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private static final String PYTHON_SCRIPT_NAME = "threp_parser_wrapper.py";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<Replay> parse(Path filePath, int gameId) {
        System.out.println("========== 开始解析 Replay ==========");
        System.out.println("文件: " + filePath);

        if (!Files.exists(filePath)) {
            System.err.println("错误：文件不存在！");
            return Optional.empty();
        }

        try {
            Path scriptPath = findPythonScript();
            if (scriptPath == null) {
                System.err.println("错误：找不到Python解析脚本");
                return Optional.empty();
            }

            Optional<JsonNode> jsonResult = callPythonParser(scriptPath, filePath);
            if (jsonResult.isEmpty()) {
                return Optional.empty();
            }

            Replay replay = parseJsonToReplay(jsonResult.get(), filePath, gameId);

            System.out.println("✓ 解析成功: " + replay.getFullShotType() + " " +
                    replay.getDifficultyDisplay() + " " + replay.getFormattedScore());

            return Optional.of(replay);

        } catch (Exception e) {
            System.err.println("✗ 解析异常: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Path findPythonScript() {
        String[] possiblePaths = {
                "parser/threp/" + PYTHON_SCRIPT_NAME,
                "../parser/threp/" + PYTHON_SCRIPT_NAME,
                "src/main/resources/parser/threp/" + PYTHON_SCRIPT_NAME,
                "threp/" + PYTHON_SCRIPT_NAME
        };

        for (String path : possiblePaths) {
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                System.out.println("找到Python脚本: " + p.toAbsolutePath());
                return p.toAbsolutePath();
            }
        }

        try {
            String classPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            Path projectRoot = Paths.get(classPath).getParent().getParent().getParent();
            Path scriptPath = projectRoot.resolve("parser/threp/" + PYTHON_SCRIPT_NAME);
            if (Files.exists(scriptPath)) {
                return scriptPath;
            }
        } catch (Exception e) {
            // ignore
        }

        return null;
    }

    private Optional<JsonNode> callPythonParser(Path scriptPath, Path replayPath) {
        try {
            String pythonCmd = System.getProperty("os.name").toLowerCase().contains("win")
                    ? "python" : "python3";

            ProcessBuilder pb = new ProcessBuilder(
                    pythonCmd,
                    scriptPath.toString(),
                    replayPath.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true);
            pb.directory(scriptPath.getParent().toFile());

            System.out.println("[1/2] 执行Python解析...");
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.err.println("错误：Python解析超时");
                return Optional.empty();
            }

            if (process.exitValue() != 0) {
                System.err.println("Python错误: " + output);
                return Optional.empty();
            }

            System.out.println("[2/2] 解析JSON数据...");
            return extractAndParseJson(output.toString());

        } catch (Exception e) {
            System.err.println("调用Python失败: " + e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<JsonNode> extractAndParseJson(String output) {
        int start = output.indexOf("###JSON_START###");
        int end = output.indexOf("###JSON_END###");

        if (start != -1 && end != -1) {
            String json = output.substring(start + 16, end).trim();
            try {
                return Optional.of(objectMapper.readTree(json));
            } catch (Exception e) {
                System.err.println("JSON解析失败: " + e.getMessage());
            }
        }

        start = output.indexOf('{');
        end = output.lastIndexOf('}');
        if (start != -1 && end != -1) {
            try {
                return Optional.of(objectMapper.readTree(output.substring(start, end + 1)));
            } catch (Exception e) {
                System.err.println("JSON解析失败: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    private Replay parseJsonToReplay(JsonNode root, Path filePath, int gameId) {
        Replay replay = new Replay();

        replay.setGameId(gameId);
        replay.setFileName(filePath.getFileName().toString());
        replay.setFilePath(filePath.toString());
        try {
            replay.setFileSize(Files.size(filePath));
        } catch (IOException e) {
            replay.setFileSize(0);
        }

        // 解析baseInfoDic
        JsonNode baseInfoDic = root.get("baseInfoDic");
        if (baseInfoDic != null) {
            replay.setCharacter(getText(baseInfoDic, "character"));
            replay.setShotType(getText(baseInfoDic, "shottype"));
            replay.setDifficulty(getText(baseInfoDic, "rank"));
            replay.setStage(getText(baseInfoDic, "stage"));
        }

        replay.setGameVersion(inferGameVersion(replay.getCharacter(), replay.getShotType()));

        // 分数信息
        JsonNode stageScoreNode = root.get("stageScore");
        if (stageScoreNode != null && stageScoreNode.isArray()) {
            List<Long> scores = new ArrayList<>();
            for (JsonNode scoreNode : stageScoreNode) {
                scores.add(scoreNode.asLong());
            }
            replay.setStageScoresList(scores);
            replay.setStageScoresJson(stageScoreNode.toString());

            if (!scores.isEmpty()) {
                replay.setTotalScore(scores.get(scores.size() - 1));
            }
        } else {
            replay.setTotalScore(root.get("totalScore").asLong(0));
        }

        replay.setTotalFrames(root.get("frameCount").asInt(0));
        replay.setCleared(root.get("cleared").asBoolean(false));

        // 日期
        String dateStr = getText(root, "date");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                replay.setGameDate(LocalDateTime.parse(dateStr, DATE_FORMATTER));
            } catch (Exception e) {
                replay.setGameDate(LocalDateTime.now());
            }
        }

        replay.setPlayerName(getText(root, "player"));
        replay.setSlowRate((float) root.get("slowRate").asDouble(0.0));

        // 炸弹统计
        JsonNode bombAnalysis = root.get("bombAnalysis");
        if (bombAnalysis != null) {
            replay.setTotalZBombs(bombAnalysis.get("totalZ").asInt(0));
            replay.setTotalXBombs(bombAnalysis.get("totalX").asInt(0));
            replay.setTotalCBombs(bombAnalysis.get("totalC").asInt(0));

            JsonNode byStageNode = bombAnalysis.get("byStage");
            if (byStageNode != null && byStageNode.isArray()) {
                List<StageBombStats> bombStats = new ArrayList<>();
                for (JsonNode stageNode : byStageNode) {
                    StageBombStats stats = new StageBombStats();
                    stats.stageNum = stageNode.get("stage").asInt(0);
                    stats.zCount = stageNode.get("z_count").asInt(0);
                    stats.xCount = stageNode.get("x_count").asInt(0);
                    stats.startFrame = stageNode.get("start_frame").asInt(0);
                    stats.endFrame = stageNode.get("end_frame").asInt(0);
                    bombStats.add(stats);
                }
                replay.setBombStatsList(bombStats);
                replay.setBombStatsJson(byStageNode.toString());
            }
        }

        replay.setRawJson(root.toString());

        return replay;
    }

    private String inferGameVersion(String character, String shotType) {
        if (character == null) return "Unknown";

        String charLower = character.toLowerCase();
        String shot = shotType != null ? shotType.toLowerCase() : "";

        if (charLower.contains("reimu") && (shot.contains("a") || shot.contains("b"))) return "TH06";
        if (charLower.contains("marisa") && (shot.contains("a") || shot.contains("b"))) return "TH06";
        if (charLower.contains("sakuya")) return "TH07";
        if (charLower.contains("youmu") || charLower.contains("yuyuko")) return "TH08";
        if (charLower.contains("reimu") && (shot.contains("c") || shot.contains("d"))) return "TH08";
        if (charLower.contains("marisa") && (shot.contains("c") || shot.contains("d"))) return "TH08";
        if (charLower.contains("reimu") && shot.contains("needle")) return "TH10";
        if (charLower.contains("marisa") && shot.contains("love")) return "TH10";
        if (charLower.contains("reimu") && shot.contains("marisa")) return "TH11";
        if (charLower.contains("sanae")) return "TH12";

        if (charLower.contains("reimu")) return "TH?? (Reimu)";
        if (charLower.contains("marisa")) return "TH?? (Marisa)";

        return "Unknown";
    }

    private String getText(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
    }
}