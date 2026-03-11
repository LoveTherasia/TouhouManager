package com.thmanager.controller;

import com.thmanager.dao.ReplayDAO;
import com.thmanager.model.Replay;
import com.thmanager.service.ReplayScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Replay文件信息REST API控制器
 * 
 * 提供Replay管理相关的REST接口，包括：
 * - 获取Replay列表（支持分页）
 * - 按游戏ID获取Replay
 * - 获取单个Replay详情
 * - 删除Replay
 * - 扫描新Replay
 * 
 * 所有接口都映射在 /api/replays 路径下。
 */
@RestController
@RequestMapping("/api/replays")
public class ReplayController {

    /**
     * Replay数据访问对象
     */
    private final ReplayDAO replayDAO;

    /**
     * Replay扫描器服务
     */
    private final ReplayScanner replayScanner;

    /**
     * 构造函数，依赖注入
     * 
     * @param replayDAO     Replay数据访问对象
     * @param replayScanner Replay扫描器服务
     */
    @Autowired
    public ReplayController(ReplayDAO replayDAO, ReplayScanner replayScanner) {
        this.replayDAO = replayDAO;
        this.replayScanner = replayScanner;
    }

    /**
     * 获取Replay列表
     * 
     * GET /api/replays
     * 
     * @param page     页码，默认为1
     * @param pageSize 每页数量，默认为0（0表示返回所有数据）
     * @return 包含数据、总数、当前页和每页数量的Map
     */
    @GetMapping
    public Map<String, Object> getReplays(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "0") int pageSize) {
        List<Replay> replays;
        int total = replayDAO.countAll();

        if (pageSize > 0) {
            replays = replayDAO.findAllPaged(page, pageSize);
        } else {
            replays = replayDAO.findAll();
        }

        return Map.of(
                "data", replays,
                "total", total,
                "page", page,
                "pageSize", pageSize);
    }

    /**
     * 按游戏ID获取Replay列表
     * 
     * GET /api/replays/game/{gameId}
     * 
     * @param gameId 游戏ID
     * @return 该游戏的所有Replay列表
     */
    @GetMapping("/game/{gameId}")
    public List<Replay> getReplaysByGame(@PathVariable int gameId) {
        return replayDAO.findByGame(gameId);
    }

    /**
     * 按ID获取单个Replay详情
     * 
     * GET /api/replays/{id}
     * 
     * @param id Replay ID
     * @return Replay对象，如果不存在则返回null
     */
    @GetMapping("/{id}")
    public Replay getReplayById(@PathVariable int id) {
        Optional<Replay> replay = replayDAO.findById(id);
        return replay.orElse(null);
    }

    /**
     * 删除指定的Replay
     * 
     * DELETE /api/replays/{id}
     * 
     * @param id Replay ID
     */
    @DeleteMapping("/{id}")
    public void deleteReplay(@PathVariable int id) {
        replayDAO.delete(id);
    }

    /**
     * 扫描所有游戏的新Replay文件
     * 
     * POST /api/replays/scan
     * 
     * @return 包含新导入Replay数量的Map
     */
    @PostMapping("/scan")
    public Map<String, Integer> scanReplays() {
        int imported = replayScanner.scanAllGames();
        return Map.of("imported", imported);
    }
}
