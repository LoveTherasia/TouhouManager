package com.thmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thmanager.model.PlaySession;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PlaySessionMapper extends BaseMapper<PlaySession> {

    @Insert("INSERT INTO play_sessions (game_id, start_time, end_time, duration_seconds, session_type, note) " +
            "VALUES (#{gameId}, #{startTime}, #{endTime}, #{durationSeconds}, #{sessionType}, #{note})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSession(PlaySession session);

    @Update("UPDATE play_sessions SET end_time = #{endTime}, duration_seconds = #{durationSeconds} WHERE id = #{sessionId}")
    int endSession(@Param("sessionId") int sessionId, @Param("endTime") LocalDateTime endTime, @Param("durationSeconds") long durationSeconds);

    @Select("SELECT * FROM play_sessions WHERE game_id = #{gameId} ORDER BY start_time DESC")
    List<PlaySession> findByGameId(@Param("gameId") int gameId);

    @Select("SELECT * FROM play_sessions ORDER BY start_time DESC LIMIT #{limit}")
    List<PlaySession> findRecent(@Param("limit") int limit);

    @Select("SELECT COALESCE(SUM(duration_seconds), 0) FROM play_sessions WHERE game_id = #{gameId} AND duration_seconds IS NOT NULL")
    long getTotalPlayTimeByGame(@Param("gameId") int gameId);
}
