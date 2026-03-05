package com.thmanager.dao;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;

//数据库管理器类
public class DatabaseManager {
    private static final String DB_NAME = "touhou_manager.db";
    private static String dbUrl;
    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // 获取数据库文件路径
    private String getDatabasePath() {
        String userHome = System.getProperty("user.home");
        Path appDir = Paths.get(userHome, ".touhou-manager");

        try {
            if (!Files.exists(appDir)) {
                Files.createDirectories(appDir);
            }
        } catch (IOException e) {
            System.err.println("无法创建目标目录" + e.getMessage());
            return DB_NAME;
        }

        return appDir.resolve(DB_NAME).toString();
    }

    private void initializeDatabase() {
        String dbPath = getDatabasePath();
        dbUrl = "jdbc:sqlite:" + dbPath;

        System.out.println("Database path: " + dbPath);

        // 加载驱动并初始化表结构
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Database connection successful");
                executeInitScript(conn);
                executeMigrationScript(conn);
            }
        } catch (SQLException e) {
            System.out.println("Failed to initialize database: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    // 执行初始化脚本
    private void  executeInitScript(Connection conn) {
        try {
            // 从resources中读取SQL文件
            InputStream is = getClass().getResourceAsStream("/database/init.sql");
            if (is == null) {
                System.err.println("找不到初始文件!,请检查resources/database/init.sql文件是否存在");
                return;
            }

            String sql = new String(is.readAllBytes(),
                    StandardCharsets.UTF_8);

            // 分割多条SQL语句执行
            String[] statements = sql.split(";");

            for (String stmt : statements) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement s = conn.createStatement()) {
                        // System.out.println(stmt.trim());
                        s.execute(trimmed);
                    }
                }
            }

            System.out.println("Database initialization successful");
        } catch (Exception e) {
            System.err.println("Failed to run initialization script: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 执行迁移脚本
    private void executeMigrationScript(Connection conn) {
        try {
            // 检查file_modified_time列是否已存在
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "replays", "file_modified_time");

            if (!columns.next()) {
                // 列不存在，执行迁移
                System.out.println("Executing database migration: adding file_modified_time column");

                InputStream is = getClass().getResourceAsStream("/database/migration_add_file_modified_time.sql");
                if (is == null) {
                    System.err.println("Migration script not found");
                    return;
                }

                String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                try (Statement s = conn.createStatement()) {
                    s.execute(sql);
                    System.out.println("Database migration successful");
                }
            } else {
                System.out.println("Database migration already applied");
            }
        } catch (SQLException e) {
            // 如果列已存在，会抛出异常，这是正常的
            if (!e.getMessage().contains("duplicate column name")) {
                System.err.println("Migration error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Failed to run migration script: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
