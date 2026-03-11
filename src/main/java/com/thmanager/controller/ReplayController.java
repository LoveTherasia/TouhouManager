package com.thmanager.controller;

import com.thmanager.dao.ReplayDAO;
import com.thmanager.model.Replay;
import com.thmanager.service.ReplayScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

//replay文件信息控制类
@RestController
@RequestMapping("/api/replays")
public class ReplayController {
    private final ReplayDAO replayDAO;

    private final ReplayScanner replayScanner;

    @Autowired
    public ReplayController(ReplayDAO replayDAO,ReplayScanner replayScanner){
        this.replayDAO=replayDAO;
        this.replayScanner=replayScanner;
    }

    //获取所有的replay
    @GetMapping
    public List<Replay> getReplays(){
        return replayDAO.findAll();
    }

    //按游戏ID获取replay
    @GetMapping("/game/{gameId}")
    public List<Replay> getReplaysByGame(@PathVariable int gameId){
        return replayDAO.findByGame(gameId);
    }

    //按ID获取单个replay
    @GetMapping("/{id}")
    public Replay getReplayById(@PathVariable int id){
        Optional<Replay> replay = replayDAO.findById(id);
        return replay.orElse(null);
    }

    //删除Replay
    @DeleteMapping("/{id}")
    public void deleteReplay(@PathVariable int id){
        replayDAO.delete(id);
    }

    //扫描新Replay
    @PostMapping("/scan")
    public Map<String,Integer> scanReplays(){
        int imported = replayScanner.scanAllGames();
        return Map.of("imported",imported);
    }
}
