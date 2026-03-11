package com.thmanager.dao;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

@Component
public class DatabaseManager {
    private static final String DB_NAME = "touhou_manager.db";

    @PostConstruct
    public void init() {
        initializeDatabase();
    }

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
        String dbUrl = "jdbc:sqlite:" + dbPath;

        System.out.println("Database path: " + dbPath);

        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            if (conn != null) {
                System.out.println("Database connection successful");
                executeInitScript(conn);
                executeMigrationScript(conn);
            }
        } catch (SQLException e) {
            System.out.println("Failed to initialize database: " + e.getMessage());
        }
    }

    private void executeInitScript(Connection conn) {
        try {
            ClassPathResource resource = new ClassPathResource("database/init.sql");
            InputStream is = resource.getInputStream();
            if (is == null) {
                System.err.println("找不到初始文件!,请检查resources/database/init.sql文件是否存在");
                return;
            }

            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] statements = sql.split(";");

            for (String stmt : statements) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty()) {
                    try (Statement s = conn.createStatement()) {
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

    private void executeMigrationScript(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "replays", "file_modified_time");

            if (!columns.next()) {
                System.out.println("Executing database migration: adding file_modified_time column");

                ClassPathResource resource = new ClassPathResource("database/migration_add_file_modified_time.sql");
                InputStream is = resource.getInputStream();
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
            if (!e.getMessage().contains("duplicate column name")) {
                System.err.println("Migration error: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Failed to run migration script: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
