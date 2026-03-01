-- 东方整数作信息表
CREATE TABLE IF NOT EXISTS games (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     game_number INTEGER NOT NULL UNIQUE,  -- 整数作编号：6=红魔乡，7=妖妖梦...
                                     title_ja TEXT NOT NULL,               -- 日文原名
                                     title_zh TEXT,                        -- 中文译名
                                     title_en TEXT,                        -- 英文缩写（如 EoSD）
                                     install_path TEXT,                    -- 游戏安装路径
                                     exe_name TEXT,                        -- 可执行文件名（如 th06.exe）
                                     replay_folder TEXT,                   -- replay文件夹路径
                                     cover_image TEXT,                     -- 封面图片路径（预留）
                                     total_play_time_seconds INTEGER DEFAULT 0,  -- 总游玩时间（秒）
                                     last_played TIMESTAMP,                -- 最后游玩时间
                                     is_installed BOOLEAN DEFAULT 0,       -- 是否已安装
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 游玩会话记录表
CREATE TABLE IF NOT EXISTS play_sessions (
                                             id INTEGER PRIMARY KEY AUTOINCREMENT,
                                             game_id INTEGER NOT NULL,
                                             start_time TIMESTAMP NOT NULL,
                                             end_time TIMESTAMP,
                                             duration_seconds INTEGER,             -- 游玩时长（秒）
                                             session_type TEXT DEFAULT 'normal',   -- 类型：normal, practice, replay
                                             note TEXT,                            -- 备注（可选）
                                             FOREIGN KEY (game_id) REFERENCES games(id)
    );

-- 创建索引优化查询
CREATE INDEX IF NOT EXISTS idx_sessions_game_id ON play_sessions(game_id);
CREATE INDEX IF NOT EXISTS idx_sessions_start_time ON play_sessions(start_time);

-- 插入东方整数作基础数据（TH06-TH19）
INSERT OR IGNORE INTO games (game_number, title_ja, title_zh, title_en, exe_name) VALUES
(6, '東方紅魔郷', '东方红魔乡', 'EoSD', 'th06.exe'),
(7, '東方妖々夢', '东方妖妖梦', 'PCB', 'th07.exe'),
(8, '東方永夜抄', '东方永夜抄', 'IN', 'th08.exe'),
(9, '東方花映塚', '东方花映冢', 'PoFV', 'th09.exe'),
(10, '東方風神録', '东方风神录', 'MoF', 'th10.exe'),
(11, '東方地霊殿', '东方地灵殿', 'SA', 'th11.exe'),
(12, '東方星蓮船', '东方星莲船', 'UFO', 'th12.exe'),
(13, '東方神霊廟', '东方神灵庙', 'TD', 'th13.exe'),
(14, '東方輝針城', '东方辉针城', 'DDC', 'th14.exe'),
(15, '東方紺珠伝', '东方绀珠传', 'LoLK', 'th15.exe'),
(16, '東方天空璋', '东方天空璋', 'HSiFS', 'th16.exe'),
(17, '東方鬼形獣', '东方鬼形兽', 'WBaWC', 'th17.exe'),
(18, '東方虹龍洞', '东方虹龙洞', 'UM', 'th18.exe'),
(19, '東方獣王園', '东方兽王园', 'UDoALG', 'th19.exe');


-- Replay数据表（完整版）
CREATE TABLE IF NOT EXISTS replays (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       game_id INTEGER NOT NULL,
                                       file_name TEXT NOT NULL,
                                       file_path TEXT NOT NULL UNIQUE,
                                       file_size INTEGER,

                                       game_version TEXT,
                                       character TEXT,
                                       shot_type TEXT,
                                       difficulty TEXT,
                                       stage TEXT,
                                       cleared BOOLEAN DEFAULT 0,
                                       total_score INTEGER DEFAULT 0,
                                       game_date TIMESTAMP,
                                       player_name TEXT,
                                       slow_rate REAL,
                                       total_frames INTEGER,

                                       stage_scores_json TEXT,
                                       bomb_stats_json TEXT,
                                       total_z_bombs INTEGER DEFAULT 0,
                                       total_x_bombs INTEGER DEFAULT 0,
                                       total_c_bombs INTEGER DEFAULT 0,

                                       raw_json TEXT,

                                       session_id INTEGER,
                                       imported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                       FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (session_id) REFERENCES play_sessions(id)
    );

CREATE INDEX IF NOT EXISTS idx_replays_game_difficulty ON replays(game_id, difficulty);
CREATE INDEX IF NOT EXISTS idx_replays_game_shot ON replays(game_id, shot_type);
CREATE INDEX IF NOT EXISTS idx_replays_score ON replays(game_id, difficulty, total_score DESC);