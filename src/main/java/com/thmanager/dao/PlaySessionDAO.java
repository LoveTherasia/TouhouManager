package com.thmanager.dao;

import com.thmanager.mapper.PlaySessionMapper;
import com.thmanager.model.PlaySession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PlaySessionDAO {
    private final PlaySessionMapper playSessionMapper;

    @Autowired
    public PlaySessionDAO(PlaySessionMapper playSessionMapper) {
        this.playSessionMapper = playSessionMapper;
    }

    public boolean create(PlaySession session) {
        int result = playSessionMapper.insertSession(session);
        return result > 0;
    }

    public boolean endSession(int sessionId, LocalDateTime endTime, long durationSeconds) {
        return playSessionMapper.endSession(sessionId, endTime, durationSeconds) > 0;
    }

    public List<PlaySession> findByGameId(int gameId) {
        return playSessionMapper.findByGameId(gameId);
    }

    public List<PlaySession> findRecent(int limit) {
        return playSessionMapper.findRecent(limit);
    }

    public long getToTotalPlayTimeByGame(int gameId) {
        return playSessionMapper.getTotalPlayTimeByGame(gameId);
    }
}
