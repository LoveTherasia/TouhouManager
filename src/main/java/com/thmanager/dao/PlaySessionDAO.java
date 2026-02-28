package com.thmanager.dao;

import com.thmanager.model.PlaySession;

import java.time.LocalDateTime;
import java.util.List;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class PlaySessionDAO {

    public boolean create(PlaySession session) {
        // 1. 插入会话数据（去掉 RETURN_GENERATED_KEYS）
        String insertSql = "INSERT INTO play_sessions (game_id,start_time,session_type,note) " +
                "VALUES (?,?,?,?)";
        // 2. 查询自增ID的SQL
        String selectIdSql = "SELECT last_insert_rowid() AS id";

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // 关闭自动提交，确保插入和查ID原子性
            conn.setAutoCommit(false);

            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                insertPstmt.setInt(1, session.getGameId());
                insertPstmt.setTimestamp(2, Timestamp.valueOf(session.getStartTime()));
                insertPstmt.setString(3, session.getSessionType());
                insertPstmt.setString(4, session.getNote());

                int affectedRows = insertPstmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }

                // 3. 查询自增ID（SQLite 专用方式）
                try (PreparedStatement selectPstmt = conn.prepareStatement(selectIdSql);
                     ResultSet rs = selectPstmt.executeQuery()) {
                    if (rs.next()) {
                        session.setId(rs.getInt("id")); // 给session设置自增ID
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Failed to create play session: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(true); // 恢复自动提交
            }
        } catch (SQLException e) {
            System.out.println("Failed to create play session: " + e.getMessage());
            return false;
        }
    }

    // 结束会话
    public boolean endSession(int sessionId, LocalDateTime endTime, long durationSeconds) {
        String sql = "UPDATE play_sessions SET end_time = ?,duration_seconds = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(endTime));
            pstmt.setLong(2, durationSeconds);
            pstmt.setInt(3, sessionId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed to end play session: " + e.getMessage());
            return false;
        }
    }

    // 查询某游戏的所有会话
    public List<PlaySession> findByGameId(int gameId) {
        List<PlaySession> sessions = new ArrayList<>();

        String sql = "SELECT * FROM play_sessions WHERE game_id = ? ORDER BY start_time DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gameId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sessions.add(mapResultSetToSessions(rs));
            }
        } catch (SQLException e) {
            System.out.println("Failed to query game play records: " + e.getMessage());
        }
        return sessions;
    }

    // 查询最近的会话
    public List<PlaySession> findRecent(int limit) {
        List<PlaySession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM play_sessions ORDER BY start_time DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sessions.add(mapResultSetToSessions(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to query recent records: " + e.getMessage());
        }
        return sessions;
    }

    // 获取某游戏的总游戏时间
    public long getToTotalPlayTimeByGame(int gameId) {
        String sql = "SELECT COALESCE(SUM(duration_seconds),0) as total FROM play_sessions " +
                "WHERE game_id = ? AND duration_seconds IS NOT NULL";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gameId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("total");
            }
        } catch (SQLException e) {
            System.out.println("Failed to play game: " + e.getMessage());
        }
        return 0;
    }

    private PlaySession mapResultSetToSessions(ResultSet rs) throws SQLException {
        PlaySession session = new PlaySession();
        session.setId(rs.getInt("id"));
        session.setGameId(rs.getInt("game_id"));
        session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());

        Timestamp endTime = rs.getTimestamp("end_time");
        if (endTime != null) {
            session.setEndTime(endTime.toLocalDateTime());
        }

        session.setDurationSeconds(rs.getLong("duration_seconds"));
        session.setSessionType(rs.getString("session_type"));
        session.setNote(rs.getString("note"));

        return session;
    }
}
