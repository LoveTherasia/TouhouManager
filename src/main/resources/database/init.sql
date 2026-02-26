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