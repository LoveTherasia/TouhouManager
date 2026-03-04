package com.thmanager.service;

import com.thmanager.dao.GameDAO;
import com.thmanager.dao.ReplayDAO;
import com.thmanager.model.Game;
import com.thmanager.model.Replay;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Replay文件夹实时监控服务
 * 自动检测新replay文件，调用Python解析，保存到数据库
 * 支持文件覆盖检测和更新
 */
@Service
public class ReplayWatcherService {

    private final GameDAO gameDAO;
    private final ReplayDAO replayDAO;
    private final ReplayParser replayParser;
    private final ExecutorService executor;
    private WatchService watchService;
    private final Map<Path, Game> watchedPaths;
    private final ConcurrentHashMap.KeySetView<String, Boolean> processingFiles;

    private Consumer<Replay> onNewReplay;
    private Consumer<String> onStatusUpdate;
    private volatile boolean running = false;

    public ReplayWatcherService(GameDAO gameDAO, ReplayDAO replayDAO) {
        this.gameDAO = gameDAO;
        this.replayDAO = replayDAO;
        this.replayParser = new ReplayParser();
        // 使用固定线程池，避免单线程被watchLoop占用
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "ReplayWatcher-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
        this.watchedPaths = new ConcurrentHashMap<>();
        this.processingFiles = ConcurrentHashMap.newKeySet();
    }

    public void setOnNewReplay(Consumer<Replay> callback) {
        this.onNewReplay = callback;
    }

    public void setOnStatusUpdate(Consumer<String> callback) {
        this.onStatusUpdate = callback;
    }

    /**
     * 启动监控所有已安装游戏
     */
    public void startWatching() {
        if (running)
            return;

        try {
            watchService = FileSystems.getDefault().newWatchService();
            running = true;

            List<Game> installedGames = gameDAO.findInstalled();
            updateStatus("开始监控 " + installedGames.size() + " 个游戏");

            for (Game game : installedGames) {
                registerGame(game);
            }

            executor.submit(this::watchLoop);

        } catch (IOException e) {
            updateStatus("启动监控失败: " + e.getMessage());
        }
    }

    /**
     * 注册单个游戏的replay文件夹
     */
    public void registerGame(Game game) {
        // 推断replay路径 - 尝试多个可能的路径
        String replayPath = game.getReplayFolder();
        if (replayPath == null && game.getInstallPath() != null) {
            // 尝试多个可能的replay文件夹名称
            String[] possibleNames = { "replay", "replays", "Replay", "Replays" };
            Path installPath = Paths.get(game.getInstallPath());

            for (String name : possibleNames) {
                Path inferred = installPath.resolve(name);
                if (Files.exists(inferred) && Files.isDirectory(inferred)) {
                    replayPath = inferred.toString();
                    game.setReplayFolder(replayPath);
                    break;
                }
            }
        }

        if (replayPath == null) {
            updateStatus("跳过 " + game.getDisplayName() + " (无replay路径)");
            return;
        }

        Path path = Paths.get(replayPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            updateStatus("无效路径: " + path);
            return;
        }

        try {
            // 检查是否已经注册过
            if (watchedPaths.containsKey(path)) {
                updateStatus("已监控: " + game.getDisplayName());
                return;
            }

            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            watchedPaths.put(path, game);
            updateStatus("已监控: " + game.getDisplayName() + " -> " + path);

            // 后台初始扫描
            executor.submit(() -> initialScan(game, path));

        } catch (IOException e) {
            updateStatus("注册失败: " + e.getMessage());
        }
    }

    /**
     * 初始扫描已有文件
     */
    private void initialScan(Game game, Path folder) {
        try {
            int count = 0, newCount = 0, updatedCount = 0;

            try (var stream = Files.list(folder)) {
                List<Path> rpyFiles = stream
                        .filter(p -> p.toString().toLowerCase().endsWith(".rpy"))
                        .filter(p -> !p.getFileName().toString().startsWith("~"))
                        .toList();

                for (Path rpy : rpyFiles) {
                    count++;
                    Optional<Replay> existing = replayDAO.findByPath(rpy.toString());

                    if (existing.isEmpty()) {
                        // 新文件，直接处理
                        if (processAndSaveReplay(rpy, game)) {
                            newCount++;
                        }
                    } else {
                        // 已存在的文件，检查是否需要更新
                        Replay oldReplay = existing.get();
                        long oldModified = oldReplay.getFileModifiedTime();
                        long newModified = Files.getLastModifiedTime(rpy).toMillis();

                        // 如果文件修改时间不同，重新处理
                        if (oldModified != newModified) {
                            System.out.println("[Watcher] 初始扫描发现文件更新: " + rpy.getFileName());
                            if (processAndSaveReplay(rpy, game)) {
                                updatedCount++;
                            }
                        }
                    }
                }
            }

            updateStatus(game.getDisplayName() + ": 扫描完成 (新: " + newCount +
                    ", 更新: " + updatedCount + ", 总计: " + count + ")");

        } catch (IOException e) {
            updateStatus("扫描失败: " + e.getMessage());
        }
    }

    /**
     * 监控循环
     */
    private void watchLoop() {
        System.out.println("[Watcher] 开始监控循环");
        while (running) {
            try {
                WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                if (key == null) {
                    continue;
                }

                Path watchDir = (Path) key.watchable();
                Game game = watchedPaths.get(watchDir);

                if (game == null) {
                    System.out.println("[Watcher] 无法找到监控目录对应的游戏: " + watchDir);
                    key.reset();
                    continue;
                }

                System.out.println("[Watcher] 收到目录事件: " + watchDir + " (游戏: " + game.getDisplayName() + ")");

                for (WatchEvent<?> event : key.pollEvents()) {
                    System.out.println("[Watcher] 事件类型: " + event.kind() + ", 上下文: " + event.context());

                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        System.out.println("[Watcher] 事件溢出，跳过");
                        continue;
                    }

                    Path fileName = (Path) event.context();
                    String fileNameStr = fileName.toString();

                    System.out.println("[Watcher] 处理文件: " + fileNameStr);

                    if (!fileNameStr.toLowerCase().endsWith(".rpy")) {
                        System.out.println("[Watcher] 不是rpy文件，跳过: " + fileNameStr);
                        continue;
                    }
                    if (fileNameStr.startsWith("~")) {
                        System.out.println("[Watcher] 是临时文件，跳过: " + fileNameStr);
                        continue;
                    }

                    Path fullPath = watchDir.resolve(fileName);
                    System.out.println("[Watcher] 完整路径: " + fullPath);
                    System.out.println("[Watcher] 准备提交任务到executor");
                    executor.submit(() -> {
                        System.out.println("[Watcher] 任务开始执行: " + fileNameStr);
                        handleNewReplay(fullPath, game);
                        System.out.println("[Watcher] 任务执行完成: " + fileNameStr);
                    });
                    System.out.println("[Watcher] 任务已提交");
                }

                key.reset();

            } catch (InterruptedException e) {
                System.err.println("[Watcher] 监控线程被中断: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[Watcher] 监控循环异常: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("[Watcher] 监控循环结束");
    }

    /**
     * 处理新replay文件（支持文件覆盖）
     */
    private void handleNewReplay(Path filePath, Game game) {
        String pathStr = filePath.toString();
        String fileName = filePath.getFileName().toString();

        System.out.println("[Watcher] 开始处理文件: " + fileName);

        // 跳过临时文件
        if (fileName.startsWith("~") || fileName.startsWith(".")) {
            System.out.println("[Watcher] 跳过临时文件: " + fileName);
            return;
        }

        // 防止重复处理
        if (!processingFiles.add(pathStr)) {
            System.out.println("[Watcher] 文件正在处理中，跳过: " + fileName);
            return;
        }

        try {
            updateStatus("检测到文件变化: " + fileName);

            // 等待文件写入完成
            System.out.println("[Watcher] 等待文件就绪: " + fileName);
            if (!waitForFileReady(filePath)) {
                updateStatus("文件未就绪，跳过: " + fileName);
                return;
            }

            // 检查文件是否真的发生了变化（使用修改时间）
            System.out.println("[Watcher] 检查文件是否变化: " + fileName);
            Optional<Replay> existing = replayDAO.findByPath(pathStr);
            if (existing.isPresent()) {
                Replay oldReplay = existing.get();
                long oldModified = oldReplay.getFileModifiedTime();
                long newModified = Files.getLastModifiedTime(filePath).toMillis();

                System.out.println("[Watcher] 旧修改时间: " + oldModified + " (" + new java.util.Date(oldModified) + ")");
                System.out.println("[Watcher] 新修改时间: " + newModified + " (" + new java.util.Date(newModified) + ")");

                // 如果修改时间相同，说明没有真正变化，跳过处理
                if (oldModified == newModified) {
                    System.out.println("[Watcher] 文件修改时间相同，跳过: " + fileName);
                    return;
                }

                System.out.println("[Watcher] 检测到文件覆盖: " + fileName +
                        " (旧修改时间: " + new java.util.Date(oldModified) +
                        " -> 新修改时间: " + new java.util.Date(newModified) + ")");
            } else {
                System.out.println("[Watcher] 发现新文件: " + fileName);
            }

            // 解析文件
            updateStatus("正在解析: " + fileName);
            System.out.println("[Watcher] 开始解析文件: " + fileName);
            Optional<Replay> parsed = replayParser.parse(filePath, game.getId());

            System.out.println("[Watcher] 解析结果: " + (parsed.isPresent() ? "成功" : "失败"));

            if (parsed.isPresent()) {
                Replay replay = parsed.get();
                // 设置文件修改时间
                replay.setFileModifiedTime(Files.getLastModifiedTime(filePath).toMillis());

                System.out.println("[Watcher] 解析成功，保存到数据库: " + fileName);
                boolean saved = replayDAO.saveOrUpdate(replay);

                if (saved) {
                    String action = existing.isPresent() ? "更新" : "导入";
                    updateStatus("✓ 已" + action + ": " + replay.getGameTitle() + " " +
                            replay.getDifficultyDisplay() + " " +
                            replay.getFormattedScore());

                    if (onNewReplay != null) {
                        System.out.println("[Watcher] 触发新replay回调");
                        onNewReplay.accept(replay);
                    }
                } else {
                    updateStatus("✗ 保存失败: " + fileName);
                }
            } else {
                updateStatus("✗ 解析失败: " + fileName);
            }

        } catch (Exception e) {
            updateStatus("✗ 处理异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            processingFiles.remove(pathStr);
            System.out.println("[Watcher] 处理完成: " + fileName);
        }
    }

    /**
     * 等待文件写入完成（改进版）
     */
    private boolean waitForFileReady(Path path) {
        long lastSize = -1;
        int stableCount = 0;
        int maxAttempts = 20; // 最多等待10秒（20次×500ms）

        System.out.println("[Watcher] 开始等待文件就绪: " + path);

        for (int i = 0; i < maxAttempts; i++) {
            try {
                if (!Files.exists(path)) {
                    System.out.println("[Watcher] 文件不存在: " + path);
                    Thread.sleep(200);
                    continue;
                }

                long size = Files.size(path);
                System.out.println("[Watcher] 尝试 " + (i + 1) + "/" + maxAttempts + ", 文件大小: " + size + " 字节");

                // 即使文件很小，也允许处理（可能是小的replay文件）
                // 文件大小稳定
                if (size == lastSize) {
                    stableCount++;
                    System.out.println("[Watcher] 文件大小稳定 (" + stableCount + "/3): " + size + " 字节");
                    if (stableCount >= 3) { // 连续3次大小相同，认为文件稳定
                        Thread.sleep(200); // 再等待200ms确保完全写入
                        System.out.println("[Watcher] 文件就绪: " + path + " (大小: " + size + ")");
                        return true;
                    }
                } else {
                    System.out.println("[Watcher] 文件大小变化: " + lastSize + " -> " + size + " 字节");
                    stableCount = 0;
                    lastSize = size;
                }

                Thread.sleep(500);

            } catch (InterruptedException e) {
                System.err.println("[Watcher] 等待被中断: " + e.getMessage());
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                System.err.println("[Watcher] 检查文件状态异常: " + e.getMessage());
                e.printStackTrace();
                // 即使出现异常，也尝试继续处理
                return true;
            }
        }

        // 超时后也尝试处理，不要直接返回false
        System.out.println("[Watcher] 等待文件就绪超时，但尝试继续处理: " + path);
        return true;
    }

    /**
     * 解析并保存单个文件（用于初始扫描）
     */
    private boolean processAndSaveReplay(Path filePath, Game game) {
        try {
            Optional<Replay> parsed = replayParser.parse(filePath, game.getId());

            if (parsed.isEmpty()) {
                return false;
            }

            Replay replay = parsed.get();
            // 设置文件修改时间
            replay.setFileModifiedTime(Files.getLastModifiedTime(filePath).toMillis());

            return replayDAO.saveOrUpdate(replay);

        } catch (Exception e) {
            updateStatus("✗ 处理异常: " + e.getMessage());
            return false;
        }
    }

    private void updateStatus(String message) {
        System.out.println("[Watcher] " + message);
        if (onStatusUpdate != null) {
            onStatusUpdate.accept(message);
        }
    }

    public void stop() {
        System.out.println("[Watcher] 正在停止监控服务...");
        running = false;

        try {
            if (watchService != null) {
                watchService.close();
                System.out.println("[Watcher] WatchService已关闭");
            }
        } catch (IOException e) {
            System.err.println("[Watcher] 关闭WatchService失败: " + e.getMessage());
        }

        watchedPaths.clear();
        processingFiles.clear();

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("[Watcher] 强制终止监控线程");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("[Watcher] Executor已关闭");
        }

        System.out.println("[Watcher] 监控服务已停止");
    }
}
