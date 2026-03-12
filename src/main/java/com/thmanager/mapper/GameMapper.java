package com.thmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thmanager.model.Game;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface GameMapper extends BaseMapper<Game> {

    @Select("SELECT * FROM games ORDER BY game_number ASC")
    List<Game> findAll();

    @Select("SELECT * FROM games WHERE id = #{id}")
    Optional<Game> findById(@Param("id") int id);

    @Update("UPDATE games SET game_number = #{gameNumber}, title_ja = #{titleJa}, title_zh = #{titleCn}, " +
            "title_en = #{titleEn}, install_path = #{installPath}, exe_name = #{exeName}, " +
            "replay_folder = #{replayFolder}, cover_image = #{coverImage}, " +
            "total_play_time_seconds = #{totalPlayTimeSeconds}, last_played = #{lastPlayed}, " +
            "is_installed = #{installed} WHERE id = #{id}")
    int updateGame(Game game);

    @Delete("DELETE FROM games WHERE id = #{id}")
    int deleteById(@Param("id") int id);

    @Select("SELECT * FROM games WHERE is_installed = 1 ORDER BY game_number ASC")
    List<Game> findInstalled();
}
