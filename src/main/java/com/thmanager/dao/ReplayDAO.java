package com.thmanager.dao;

import com.thmanager.model.Replay;
import com.thmanager.model.Replay.StageBombStats;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

//Replay信息管理类
@Repository
public class ReplayDAO {

    public boolean saveOrUpdate(Replay replay) {
        Optional<Replay> existing = findByPath(replay.getFilePath());
        if (existing.isPresent()) {
            System.out.println("Replay已存在，更新: " + replay.getFilePath());
            return update(replay);
        }
        return create(replay);
    }

    public boolean update(Replay replay) {
        String sql = "UPDATE replays SET game_version = ?, character = ?, shot_type = ?, " +
                "difficulty = ?, stage = ?, cleared = ?, total_score = ?, game_date = ?, " +
                "player_name = ?, slow_rate = ?, total_frames = ?, stage_scores_json = ?, " +
                "bomb_stats_json = ?, total_z_bombs = ?, total_x_bombs = ?, total_c_bombs = ?, " +
                "raw_json = ?, file_size = ?, file_modified_time = ? WHERE file_path = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, replay.getGameVersion());
            pstmt.setString(2, replay.getCharacter());
            pstmt.setString(3, replay.getShotType());
            pstmt.setString(4, replay.getDifficulty());
            pstmt.setString(5, replay.getStage());
            pstmt.setBoolean(6, replay.isCleared());
            pstmt.setLong(7, replay.getTotalScore());

            if (replay.getGameDate() != null) {
                pstmt.setTimestamp(8, Timestamp.valueOf(replay.getGameDate()));
            } else {
                pstmt.setNull(8, Types.TIMESTAMP);
            }

            pstmt.setString(9, replay.getPlayerName());
            pstmt.setFloat(10, replay.getSlowRate());
            pstmt.setInt(11, replay.getTotalFrames());
            pstmt.setString(12, replay.getStageScoresJson());
            pstmt.setString(13, replay.getBombStatsJson());
            pstmt.setInt(14, replay.getTotalZBombs());
            pstmt.setInt(15, replay.getTotalXBombs());
            pstmt.setInt(16, replay.getTotalCBombs());
            pstmt.setString(17, replay.getRawJson());
            pstmt.setLong(18, replay.getFileSize());
            pstmt.setLong(19, replay.getFileModifiedTime());
            pstmt.setString(20, replay.getFilePath());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✓ Replay已更新: " + replay.getFileName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("更新Replay失败: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean create(Replay replay) {
        String sql = "INSERT INTO replays (game_id, file_name, file_path, file_size, file_modified_time, " +
                "game_version, character, shot_type, difficulty, stage, cleared, " +
                "total_score, game_date, player_name, slow_rate, total_frames, " +
                "stage_scores_json, bomb_stats_json, total_z_bombs, total_x_bombs, " +
                "total_c_bombs, raw_json, session_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) { // 移除 Statement.RETURN_GENERATED_KEYS

            pstmt.setInt(1, replay.getGameId());
            pstmt.setString(2, replay.getFileName());
            pstmt.setString(3, replay.getFilePath());
            pstmt.setLong(4, replay.getFileSize());
            pstmt.setLong(5, replay.getFileModifiedTime());
            pstmt.setString(6, replay.getGameVersion());
            pstmt.setString(7, replay.getCharacter());
            pstmt.setString(8, replay.getShotType());
            pstmt.setString(9, replay.getDifficulty());
            pstmt.setString(10, replay.getStage());
            pstmt.setBoolean(11, replay.isCleared());
            pstmt.setLong(12, replay.getTotalScore());

            if (replay.getGameDate() != null) {
                pstmt.setTimestamp(13, Timestamp.valueOf(replay.getGameDate()));
            } else {
                pstmt.setNull(13, Types.TIMESTAMP);
            }

            pstmt.setString(14, replay.getPlayerName());
            pstmt.setFloat(15, replay.getSlowRate());
            pstmt.setInt(16, replay.getTotalFrames());
            pstmt.setString(17, replay.getStageScoresJson());
            pstmt.setString(18, replay.getBombStatsJson());
            pstmt.setInt(19, replay.getTotalZBombs());
            pstmt.setInt(20, replay.getTotalXBombs());
            pstmt.setInt(21, replay.getTotalCBombs());
            pstmt.setString(22, replay.getRawJson());

            if (replay.getSessionId() != null) {
                pstmt.setInt(23, replay.getSessionId());
            } else {
                pstmt.setNull(23, Types.INTEGER);
            }

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                // 不获取生成键，SQLite不支持
                // 如果需要id，可以通过其他方式查询
                System.out.println("✓ Replay已保存: " + replay.getFileName());
                return true;
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false;
            }
            System.err.println("保存Replay失败: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Optional<Replay> findByPath(String filePath) {
        String sql = "SELECT r.*, g.title_zh as game_title FROM replays r " +
                "JOIN games g ON r.game_id = g.id WHERE r.file_path = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, filePath);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询Replay失败: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Replay> findByGame(int gameId) {
        List<Replay> list = new ArrayList<>();
        String sql = "SELECT r.*, g.title_zh as game_title FROM replays r " +
                "JOIN games g ON r.game_id = g.id WHERE r.game_id = ? " +
                "ORDER BY r.imported_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gameId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询游戏Replay失败: " + e.getMessage());
        }
        return list;
    }

    public Optional<Replay> findBestByDifficulty(int gameId, String difficulty) {
        String sql = "SELECT r.*, g.title_zh as game_title FROM replays r " +
                "JOIN games g ON r.game_id = g.id " +
                "WHERE r.game_id = ? AND r.difficulty = ? " +
                "ORDER BY r.total_score DESC LIMIT 1";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gameId);
            pstmt.setString(2, difficulty);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询最高分失败: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Replay> findRecent(int limit) {
        List<Replay> list = new ArrayList<>();
        String sql = "SELECT r.*, g.title_zh as game_title FROM replays r " +
                "JOIN games g ON r.game_id = g.id " +
                "ORDER BY r.imported_at DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Replay> findAll() {
        List<Replay> list = new ArrayList<>();
        String sql = "SELECT r.*, g.title_zh as game_title FROM replays r " +
                "JOIN games g ON r.game_id = g.id " +
                "ORDER BY r.imported_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询所有Replay失败: " + e.getMessage());
        }
        return list;
    }

    public Optional<Replay> findById(int id) {
        String sql = "SELECT r.*, g.title_zh as game_title FROM replays r " +
                "JOIN games g ON r.game_id = g.id WHERE r.id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询Replay失败: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM replays WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("删除Replay失败: " + e.getMessage());
        }
        return false;
    }

    private Replay mapResultSet(ResultSet rs) throws SQLException {
        Replay r = new Replay();
        r.setId(rs.getInt("id"));
        r.setGameId(rs.getInt("game_id"));
        r.setFileName(rs.getString("file_name"));
        r.setFilePath(rs.getString("file_path"));
        r.setFileSize(rs.getLong("file_size"));
        r.setFileModifiedTime(rs.getLong("file_modified_time"));
        r.setGameVersion(rs.getString("game_version"));
        r.setCharacter(rs.getString("character"));
        r.setShotType(rs.getString("shot_type"));
        r.setDifficulty(rs.getString("difficulty"));
        r.setStage(rs.getString("stage"));
        r.setCleared(rs.getBoolean("cleared"));
        r.setTotalScore(rs.getLong("total_score"));

        Timestamp gameDate = rs.getTimestamp("game_date");
        if (gameDate != null)
            r.setGameDate(gameDate.toLocalDateTime());

        r.setPlayerName(rs.getString("player_name"));
        r.setSlowRate(rs.getFloat("slow_rate"));
        r.setTotalFrames(rs.getInt("total_frames"));
        r.setStageScoresJson(rs.getString("stage_scores_json"));
        r.setBombStatsJson(rs.getString("bomb_stats_json"));
        r.setTotalZBombs(rs.getInt("total_z_bombs"));
        r.setTotalXBombs(rs.getInt("total_x_bombs"));
        r.setTotalCBombs(rs.getInt("total_c_bombs"));
        r.setRawJson(rs.getString("raw_json"));

        int sessionId = rs.getInt("session_id");
        if (!rs.wasNull())
            r.setSessionId(sessionId);

        Timestamp imported = rs.getTimestamp("imported_at");
        if (imported != null)
            r.setImportedAt(imported.toLocalDateTime());

        r.setGameTitle(rs.getString("game_title"));

        return r;
    }
}