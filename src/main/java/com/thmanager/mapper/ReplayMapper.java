package com.thmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thmanager.model.Replay;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

/**
 * Replay MyBatis Mapper接口
 * 
 * 提供R eplay实体的数据库操作映射，通过注解方式定义SQL语句。
 * 所有查询都会JOIN games表来获取游戏标题。
 * 
 * @Mapper 注解表示这是一个MyBatis Mapper接口。
 * @extends BaseMapper 继承MyBatis-Plus提供的基础CRUD方法。
 */ 
@Mapper
public interface ReplayMapper extends BaseMapper<Replay> {

    /**
     * 通过文件路径获取Replay数据（包含游戏标题）
     *  
     * @param filePath Replay文件完整路径
     * @return 包含Replay的Optional对象
     */
    @Select("SELECT r.*, g.title_zh as game_title FROM replays r " +
            "JOIN games g ON r.game_id = g.id WHERE r.file_path = #{filePath}")
            
    Optional<Replay> findByPathWithGameTitle(@Param("filePath") String filePath);
 
    /**

            
     * @param gameId 游戏ID
     * @return 该游戏的Replay列表，按导入时间降序排列
     */
    @Select("SELECT r.*, g.title_zh as game_title FROM replays r " +
            "JOIN games g ON r.game_id = g.id WHERE r.game_id = #{gameId} " +
            "ORDER BY r.imported_at DESC")
    List<Replay> findByGameIdWithGameTitle(@Param("gameId") int gameId);

    /**
     * 通过游戏ID和难度找到对应最高分Replay（包含游戏标题）
     * 
     * @param gameId 游戏ID
     * @param difficulty 难度
     * @return 包含最高分Replay的Optional对象
     */
            @Select("SELECT r.*, g.title_zh as game_title FROM replays r " +
            "JOIN games g ON r.game_id = g.id " +
            "WHERE r.game_id = #{gameId} AND r.difficulty = #{difficulty} " +
             "ORDER BY r.total_score DESC LIMIT 1")
    Optional<Replay> findBestByDifficultyWithGameTitle(@Param("gameId") int gameId, @Param("difficulty") String difficulty);

    /**
     * 查找最近的游戏记录（包含游戏标题）
     *  
     * @param limit 返回的最大数量
     * @return 最近导入的Replay列表
     */
    @Select("SELECT r.*, g.title_zh as game_title FROM replays r " +
            "JOIN games g ON r.game_id = g.id " +
            "ORDER BY r.imported_at DESC LIMIT #{limit}")
    List<Replay> findRecentWithGameTitle(@Param("limit") int limit);

    /**
     * 找到所有的游戏记录（包含游戏标题）
     * 
     *  @return 所有Replay列表，按导入时间降序排列
     */
    @Select("SELECT r.*, g.title_zh as game_title FROM replays r " +
            "JOIN games g ON r.game_id = g.id " +
            "ORDER BY r.imported_at DESC")
            
    List<Replay> findAllWithGameTitle();

    /**
     * 获取Replay总数
     * 
     *  @return Replay记录总数
     */
    @Select("SELECT COUNT(*) FROM replays")
    int countAll();

    /**
     * 分页查询所有Replay（包含游戏标题）
     * 
     * @param offset 偏移量
     * @param pageSize 每页数量
     * @return 分页的Replay列表
     */
    @Select("SELECT r.*, g.title_zh as game_title FROM replays r " +
            "JOIN games g ON r.game_id = g.id " +
            "ORDER BY r.imported_at DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<Replay> findAllWithGameTitlePaged(@Param("offset") int offset, @Param("pageSize") int pageSize);

    /**
     * 通过ID查找游戏记录（包含游戏标题）
     * 
     * @param id Replay ID
     * @return 包含Replay的Optional对象
     */
    @Select("SELECT r.*, g.title_zh as game_title FROM replays r " +
            "JOIN games g ON r.game_id = g.id WHERE r.id = #{id}")
    Optional<Replay> findByIdWithGameTitle(@Param("id") int id);

    /**
     * 插入新的Replay记录
     * 
     * @param replay 要插入的Replay对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO replays (game_id, file_name, file_path, file_size, file_modified_time, " +
            "game_version, character, shot_type, difficulty, stage, cleared, total_score, " +
            "game_date, player_name, slow_rate, total_frames, stage_scores_json, bomb_stats_json, " +
            "total_z_bombs, total_x_bombs, total_c_bombs, raw_json, session_id, imported_at) " +
            "VALUES (#{gameId}, #{fileName}, #{filePath}, #{fileSize}, #{fileModifiedTime}, " +
            "#{gameVersion}, #{character}, #{shotType}, #{difficulty}, #{stage}, #{cleared}, #{totalScore}, " +
            "#{gameDate}, #{playerName}, #{slowRate}, #{totalFrames}, #{stageScoresJson}, #{bombStatsJson}, " +
            "#{totalZBombs}, #{totalXBombs}, #{totalCBombs}, #{rawJson}, #{sessionId}, #{importedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertReplay(Replay replay);

    /**
     * 更新Replay记录
     * 
     * @param replay 要更新的Replay对象（必须包含id）
     * @return 影响的行数
     */
    @Update("UPDATE replays SET game_id = #{gameId}, file_name = #{fileName}, file_path = #{filePath}, " +
            "file_size = #{fileSize}, file_modified_time = #{fileModifiedTime}, game_version = #{gameVersion}, " +
            "character = #{character}, shot_type = #{shotType}, difficulty = #{difficulty}, stage = #{stage}, " +
            "cleared = #{cleared}, total_score = #{totalScore}, game_date = #{gameDate}, player_name = #{playerName}, " +
            "slow_rate = #{slowRate}, total_frames = #{totalFrames}, stage_scores_json = #{stageScoresJson}, " +
            "bomb_stats_json = #{bombStatsJson}, total_z_bombs = #{totalZBombs}, total_x_bombs = #{totalXBombs}, " +
            "total_c_bombs = #{totalCBombs}, raw_json = #{rawJson}, session_id = #{sessionId} WHERE id = #{id}")
    int updateReplay(Replay replay);

    /**
     * 删除Replay记录
     * 
     * @param id 要删除的Replay ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM replays WHERE id = #{id}")
    int deleteReplayById(@Param("id") int id);
}
