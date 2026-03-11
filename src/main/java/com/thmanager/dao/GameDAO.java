package com.thmanager.dao;

import com.thmanager.mapper.GameMapper;
import com.thmanager.model.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GameDAO {
    private final GameMapper gameMapper;

    @Autowired
    public GameDAO(GameMapper gameMapper) {
        this.gameMapper = gameMapper;
    }

    public List<Game> findAll() {
        return gameMapper.findAll();
    }

    public Optional<Game> findById(int id) {
        return gameMapper.findById(id);
    }

    public boolean update(Game game) {
        return gameMapper.updateGame(game) > 0;
    }

    public boolean Delete(int id) {
        return gameMapper.deleteById(id) > 0;
    }

    public List<Game> findInstalled() {
        return gameMapper.findInstalled();
    }
}


       