package com.thmanager.service;

import com.thmanager.model.Replay;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 东方Replay文件解析器 - 调用Python threp库版本
 * 通过调用 threp_example.py 解析 replay 文件
 */
public class ReplayParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter THREP_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    // Python脚本路径（相对于项目根目录）
    private static final String PYTHON_SCRIPT_PATH = "parser/threp/threp_parser_wrapper.py";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 核心解析方法 - 调用Python threp库
     */
    public Optional<Replay> parse(Path filePath) {
        System.out.println("========== 开始解析 ==========");
        System.out.println("Parsing file: " + filePath);

        if (!Files.exists(filePath)) {
            System.err.println("错误：文件不存在！");
            return Optional.empty();
        }

        // 调用Python脚本解析
        Optional<Replay> replayOpt = callPythonParser(filePath);

        if (replayOpt.isPresent()) {
            System.out.println("✓ 解析成功！");
            Replay replay = replayOpt.get();

            // 保存解析信息到文件
            try {
                Path infoPath = filePath.getParent().resolve(filePath.getFileName() + "_info.txt");
                saveReplayInfoToFile(replay, infoPath);
                System.out.println("✓ 解析信息已保存到：" + infoPath);
            } catch (IOException e) {
                System.err.println("✗ 保存文件失败：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("✗ 解析失败");
        }

        System.out.println("========== 解析结束 ==========");
        return replayOpt;
    }

    /**
     * 调用Python threp解析器
     */
    private Optional<Replay> callPythonParser(Path filePath) {
        try {
            // 构建Python命令
            // 假设python在环境变量中，或者使用python3
            String pythonCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";

            // 获取项目根目录（假设当前工作目录是项目根目录）
            Path projectRoot = Paths.get("").toAbsolutePath();
            Path scriptPath = projectRoot.resolve(PYTHON_SCRIPT_PATH);

            // 如果脚本不存在，尝试使用类路径相对路径
            if (!Files.exists(scriptPath)) {
                // 尝试从当前类路径推断
                String classPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
                Path servicePath = Paths.get(classPath).getParent(); // /service
                Path mainPath = servicePath.getParent(); // /java
                Path srcPath = mainPath.getParent().getParent(); // /src
                projectRoot = srcPath.getParent(); // 项目根目录
                scriptPath = projectRoot.resolve(PYTHON_SCRIPT_PATH);
            }

            System.out.println("[1/3] 准备调用Python解析器...");
            System.out.println("  Python脚本路径: " + scriptPath);
            System.out.println("  Replay文件路径: " + filePath.toAbsolutePath());

            if (!Files.exists(scriptPath)) {
                System.err.println("  错误：找不到Python脚本: " + scriptPath);
                // 尝试创建wrapper脚本
                createPythonWrapper(scriptPath.getParent());
            }

            // 构建进程
            ProcessBuilder pb = new ProcessBuilder(
                    pythonCmd,
                    scriptPath.toString(),
                    filePath.toAbsolutePath().toString()
            );

            pb.redirectErrorStream(true); // 合并错误流
            pb.directory(projectRoot.toFile()); // 设置工作目录

            System.out.println("[2/3] 执行Python解析...");
            Process process = pb.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待进程完成
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                System.err.println("  错误：Python解析超时");
                return Optional.empty();
            }

            int exitCode = process.exitValue();
            System.out.println("  Python进程退出码: " + exitCode);

            if (exitCode != 0) {
                System.err.println("  Python错误输出:\n" + output);
                return Optional.empty();
            }

            System.out.println("[3/3] 解析Python返回的JSON数据...");
            return parsePythonOutput(output.toString(), filePath);

        } catch (Exception e) {
            System.err.println("✗ 调用Python解析器异常: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * 解析Python输出的JSON数据
     */
    private Optional<Replay> parsePythonOutput(String jsonOutput, Path filePath) {
        try {
            // 提取JSON部分（Python可能输出其他日志信息）
            String jsonStr = extractJson(jsonOutput);
            if (jsonStr == null) {
                System.err.println("  错误：无法从输出中提取JSON数据");
                System.err.println("  原始输出:\n" + jsonOutput);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(jsonStr);

            if (root.has("error")) {
                System.err.println("  Python解析错误: " + root.get("error").asText());
                return Optional.empty();
            }

            Replay replay = new Replay();
            replay.setFileName(filePath.getFileName().toString());
            replay.setFilePath(filePath.toString());
            replay.setFileSize(Files.size(filePath));

            // 解析基本信息字典
            JsonNode baseInfoDic = root.get("baseInfoDic");
            if (baseInfoDic != null) {
                String character = getTextOrDefault(baseInfoDic, "character", "Unknown");
                String shottype = getTextOrDefault(baseInfoDic, "shottype", "");
                String rank = getTextOrDefault(baseInfoDic, "rank", "Unknown");
                String stage = getTextOrDefault(baseInfoDic, "stage", "Unknown");

                replay.setShotType(character + (shottype.isEmpty() ? "" : " " + shottype));
                replay.setDifficulty(rank);
                replay.setStage(stage);

                // 推断游戏版本（根据机体特征）
                replay.setGameVersion(inferGameVersion(character, shottype));
            }

            // 解析玩家名
            replay.setPlayerName(getTextOrDefault(root, "player", "Unknown"));

            // 解析处理落
            replay.setSlowRate((float) (root.has("slowRate") ? root.get("slowRate").asDouble() : 0.0));

            // 解析日期
            String dateStr = getTextOrDefault(root, "date", null);
            if (dateStr != null) {
                try {
                    // threp格式: 2015/02/17 22:23
                    LocalDateTime date = LocalDateTime.parse(dateStr, THREP_DATE_FORMATTER);
                    replay.setDate(date);
                } catch (Exception e) {
                    replay.setDate(LocalDateTime.now());
                }
            } else {
                replay.setDate(LocalDateTime.now());
            }

            // 解析分数（取最后一面的分数作为总分）
            JsonNode stageScoreNode = root.get("stageScore");
            long totalScore = 0;
            if (stageScoreNode != null && stageScoreNode.isArray() && stageScoreNode.size() > 0) {
                List<Long> stageScores = new ArrayList<>();
                for (JsonNode scoreNode : stageScoreNode) {
                    stageScores.add(scoreNode.asLong());
                }
                totalScore = stageScores.get(stageScores.size() - 1); // 最后一面的分数
            }
            replay.setScore(totalScore);

            // 解析帧数
            replay.setFrameCount(root.has("frameCount") ? root.get("frameCount").asInt() : 0);

            // 解析错误信息
            JsonNode errorsNode = root.get("errors");
            if (errorsNode != null && errorsNode.isArray()) {
                for (JsonNode errorNode : errorsNode) {
                    replay.addError(errorNode.toString());
                }
            }

            // 保存原始JSON数据供后续使用
            replay.setRawJson(jsonStr);

            System.out.println("  解析结果:");
            System.out.println("    游戏: " + replay.getGameVersion());
            System.out.println("    机体: " + replay.getShotType());
            System.out.println("    难度: " + replay.getDifficulty());
            System.out.println("    分数: " + String.format("%,d", replay.getScore()));

            return Optional.of(replay);

        } catch (Exception e) {
            System.err.println("  解析JSON数据异常: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * 从输出中提取JSON字符串
     */
    private String extractJson(String output) {
        // 查找JSON_START和JSON_END标记，或者直接找第一个'{'
        int start = output.indexOf("###JSON_START###");
        int end = output.indexOf("###JSON_END###");

        if (start != -1 && end != -1) {
            return output.substring(start + 16, end).trim();
        }

        // 尝试直接解析整个输出为JSON
        start = output.indexOf('{');
        end = output.lastIndexOf('}');

        if (start != -1 && end != -1 && end > start) {
            return output.substring(start, end + 1);
        }

        return null;
    }

    /**
     * 根据机体推断游戏版本
     */
    private String inferGameVersion(String character, String shottype) {
        String charLower = character.toLowerCase();

        // TH06 红魔乡
        if (charLower.contains("reimu") && (shottype.contains("A") || shottype.contains("B"))) {
            if (shottype.contains("A") || shottype.contains("B")) return "TH06";
        }
        if (charLower.contains("marisa") && (shottype.contains("A") || shottype.contains("B"))) {
            return "TH06";
        }

        // TH07 妖妖梦
        if (charLower.contains("sakuya")) return "TH07";

        // TH08 永夜抄
        if (charLower.contains("youmu") || charLower.contains("yuyuko")) return "TH08";
        if (charLower.contains("reimu") && (shottype.contains("C") || shottype.contains("D"))) return "TH08";
        if (charLower.contains("marisa") && (shottype.contains("C") || shottype.contains("D"))) return "TH08";

        // TH10 风神录
        if (charLower.contains("reimu") && shottype.toLowerCase().contains("needle")) return "TH10";

        // 默认
        return "TH??";
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return defaultValue;
    }

    /**
     * 创建Python wrapper脚本（如果不存在）
     */
    private void createPythonWrapper(Path scriptDir) throws IOException {
        System.out.println("  正在创建Python wrapper脚本...");

        if (!Files.exists(scriptDir)) {
            Files.createDirectories(scriptDir);
        }

        Path wrapperPath = scriptDir.resolve("threp_parser_wrapper.py");

        String wrapperContent = """
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
\"\"\"
东方Replay解析器 - Python Wrapper
调用threp库解析replay文件，输出JSON格式结果
\"\"\"

import sys
import json
import os

# 添加threp库路径
script_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(script_dir))

try:
    from threp import THReplay
except ImportError as e:
    print(json.dumps({"error": f"无法导入threp库: {str(e)}"}))
    sys.exit(1)

def parse_replay(file_path):
    try:
        tr = THReplay(file_path)
        
        result = {
            "baseInfo": tr.getBaseInfo(),
            "baseInfoDic": tr.getBaseInfoDic(),
            "stageScore": tr.getStageScore(),
            "player": tr.getPlayer(),
            "slowRate": tr.getSlowRate(),
            "date": tr.getDate(),
            "errors": tr.getError(),
            "frameCount": tr.getFrameCount(),
            "z_frames": tr.getZ(),
            "x_frames": tr.getX(),
            "c_frames": tr.getC(),
            "shift_frames": tr.getShift()
        }
        
        return result
        
    except Exception as e:
        return {"error": str(e)}

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(json.dumps({"error": "缺少replay文件路径参数"}))
        sys.exit(1)
    
    replay_path = sys.argv[1]
    
    if not os.path.exists(replay_path):
        print(json.dumps({"error": f"文件不存在: {replay_path}"}))
        sys.exit(1)
    
    result = parse_replay(replay_path)
    
    # 输出JSON标记，方便Java提取
    print("###JSON_START###")
    print(json.dumps(result, ensure_ascii=False, indent=2))
    print("###JSON_END###")
""";

        Files.write(wrapperPath, wrapperContent.getBytes(StandardCharsets.UTF_8));
        System.out.println("  已创建: " + wrapperPath);
    }

    // ========== 保存格式化的解析信息 ==========
    private void saveReplayInfoToFile(Replay replay, Path outputPath) throws IOException {
        String content = formatReplayToText(replay);
        try (BufferedWriter writer = Files.newBufferedWriter(
                outputPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            writer.write(content);
        }
    }

    // ========== 格式化解密信息 ==========
    private String formatReplayToText(Replay replay) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== 东方Replay文件解析结果 =====\\n\\n");

        sb.append("【文件信息】\\n");
        sb.append("文件名：").append(replay.getFileName()).append("\\n");
        sb.append("文件路径：").append(replay.getFilePath()).append("\\n");
        sb.append("文件大小：").append(replay.getFileSize()).append(" 字节\\n\\n");

        sb.append("【游戏信息】\\n");
        sb.append("游戏版本：").append(replay.getGameVersion() == null ? "未知" : replay.getGameVersion()).append("\\n");
        sb.append("难度：").append(replay.getDifficulty() == null ? "未知" : replay.getDifficulty()).append("\\n");
        sb.append("机体：").append(replay.getShotType() == null ? "未知" : replay.getShotType()).append("\\n");
        sb.append("到达面数：").append(replay.getStage() == null ? "未知" : replay.getStage()).append("\\n");
        sb.append("分数：").append(String.format("%,d", replay.getScore())).append("\\n");
        sb.append("玩家名：").append(replay.getPlayerName() == null ? "未知" : replay.getPlayerName()).append("\\n");
        sb.append("慢放率：").append(String.format("%.3f", replay.getSlowRate())).append("\\n");
        sb.append("游戏日期：").append(
                replay.getDate() == null ? "未知" : replay.getDate().format(DATE_FORMATTER)
        ).append("\\n");
        sb.append("总帧数：").append(replay.getFrameCount()).append("\\n\\n");

        if (replay.hasErrors()) {
            sb.append("【解析错误信息】\\n");
            for (String error : replay.getErrors()) {
                sb.append("- ").append(error).append("\\n");
            }
            sb.append("\\n");
        }

        sb.append("===== 解析完成 =====\\n");
        sb.append("解析时间：").append(LocalDateTime.now().format(DATE_FORMATTER));

        return sb.toString();
    }
}