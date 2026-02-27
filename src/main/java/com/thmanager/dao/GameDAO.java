package com.thmanager.dao;

import com.thmanager.model.Game;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class GameDAO {
    //查询所有游戏
    public List<Game> findAll(){
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games ORDER BY game_number";

        try(Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            while(rs.next()){
                games.add(mapResultSetToGame(rs));
            }
        }catch(SQLException e){
            System.out.println("查询游戏列表失败:" + e.getMessage());
        }
        return games;
    }

    public Optional<Game> findById(int id){
        String sql = "SELECT * FROM games WHERE id = ?";

        try(Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1,id);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                return Optional.of(mapResultSetToGame(rs));
            }
        }catch(SQLException e){
            System.out.println("查询游戏失败" + e.getMessage());
        }

        return Optional.empty();
    }

    //更新游戏信息
    public boolean update(Game game){
        String sql = "UPDATE games SET install_path = ?,replay_folder = ?, " +
                "total_play_time_seconds = ?,last_played = ?,is_installed  = ? WHERE id = ?";

        try(Connection conn = DatabaseManager.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,game.getInstallPath());
            pstmt.setString(2,game.getReplayFolder());
            pstmt.setLong(3,game.getTotalPlayTimeSeconds());

            if(game.getLastPlayed() != null){
                pstmt.setTimestamp(4,Timestamp.valueOf(game.getLastPlayed()));
            }else{
                pstmt.setNull(4,Types.TIMESTAMP);
            }

            pstmt.setBoolean(5,game.isInstalled());
            pstmt.setInt(6,game.getId());

            return pstmt.executeUpdate() > 0;
        }catch(SQLException e){
            System.err.println("更新游戏信息失败" + e.getMessage());
            return false;
        }
    }

    //获取已安装的游戏
    public List<Game> findInstalled(){
        List<Game> games = new ArrayList<>();
        String sql = "SELECT * FROM games WHERE is_installed = 1 ORDER BY game_number";

        try(Connection conn = DatabaseManager.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){

            while(rs.next()){
                games.add(mapResultSetToGame(rs));
            }
        }catch(SQLException e){
            System.err.println("查询已安装游戏失败" + e.getMessage());
        }

        return games;
    }

    //将ResultSet映射到Game对象
    private Game mapResultSetToGame(ResultSet rs) throws SQLException {
        Game game = new Game();
        game.setId(rs.getInt("id"));
        game.setGameNumber(rs.getInt("game_number"));
        game.setTitleJa(rs.getString("title_ja"));
        game.setTitleCn(rs.getString("title_zh"));
        game.setTitleEn(rs.getString("title_en"));
        game.setInstallPath(rs.getString("install_path"));
        game.setExeName(rs.getString("exe_name"));
        game.setReplayFolder(rs.getString("replay_folder"));
        game.setCoverImage(rs.getString("cover_image"));
        game.setTotalPlayTimeSeconds(rs.getLong("total_play_time_seconds"));

        Timestamp lastPlayed = rs.getTimestamp("last_played");

        if(lastPlayed != null){
            game.setLastPlayed(lastPlayed.toLocalDateTime());
        }

        game.setInstalled(rs.getBoolean("is_installed"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if(createdAt != null){
            game.setCreatedAt(createdAt.toLocalDateTime());
        }

        return game;
    }
}
