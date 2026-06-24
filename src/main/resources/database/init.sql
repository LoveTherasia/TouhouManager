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
                                      description TEXT,                     -- 游戏简介/剧情描述
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
INSERT OR IGNORE INTO games (game_number, title_ja, title_zh, title_en, exe_name, description) VALUES
(6, '東方紅魔郷', '东方红魔乡', 'EoSD', 'th06.exe', '博丽灵梦调查红魔馆的异变，面对吸血鬼姐妹蕾米莉亚和芙兰朵露，在夏日幻想乡展开弹幕对决'),
(7, '東方妖々夢', '东方妖妖梦', 'PCB', 'th07.exe', '春雪异变笼罩幻想乡，灵梦与魔理沙深入冥界，揭开西行寺幽幽子与八云紫背后的秘密'),
(8, '東方永夜抄', '东方永夜抄', 'IN', 'th08.exe', '不老的不夜城，永远亭的公主与月之使者卷入永恒之夜，众角色在迷途竹林中探索真相'),
(9, '東方花映塚', '东方花映冢', 'PoFV', 'th09.exe', '幻想乡被大量花朵覆盖，亡灵与妖怪争夺樱花树下的春度，在生死交错的弹幕中展开对决'),
(10, '東方風神録', '东方风神录', 'MoF', 'th10.exe', '山上的神社搬来外界神明，守矢一族与八坂神奈子试图收集信仰，引发幻想乡的信仰之争'),
(11, '東方地霊殿', '东方地灵殿', 'SA', 'th11.exe', '地底灼热地狱中怨灵暴走，古明地觉与古明地恋姐妹之间的情感纠葛在地下世界引发异变'),
(12, '東方星蓮船', '东方星莲船', 'UFO', 'th12.exe', '神奇的宝船出现在天空之上，揭秘被封印的魔法与僧侣，在云层之上展开神秘旅程'),
(13, '東方神霊廟', '东方神灵庙', 'TD', 'th13.exe', '三角圣域中的仙人复归，道士宫古芳香与霍青娥展开的复活亡灵计划导致大量魂魄彷徨'),
(14, '東方輝針城', '东方辉针城', 'DDC', 'th14.exe', '小人族企图利用万宝槌的力量向人类复仇，在无数弹幕的逃亡与战斗中逐渐揭开真相'),
(15, '東方紺珠伝', '东方绀珠传', 'LoLK', 'th15.exe', '月之都的入侵计划展开，纯狐与月之民之间的千年恩怨在永恒的战斗中被揭露与解明'),
(16, '東方天空璋', '东方天空璋', 'HSiFS', 'th16.exe', '四季异变使幻想乡陷入混乱，三位妖精与隐岐奈展开抗争，自然之力的冲突与调和'),
(17, '東方鬼形獣', '东方鬼形兽', 'WBaWC', 'th17.exe', '畜生界发生剧烈动荡，动物灵与埴轮灵的对抗引发地狱变化，信仰与灵魂的较量'),
(18, '東方虹龍洞', '东方虹龙洞', 'UM', 'th18.exe', '神秘卡片在幻想乡流通，大天狗饭纲丸龙策划的阴谋在虹龙洞展开，收藏与战斗交织'),
(19, '東方獣王園', '东方兽王园', 'UDoALG', 'th19.exe', '猿田彦神计划征服幻想乡，召唤各类野兽灵魂进行驯服，展开全新的弹幕之战');


-- Replay数据表（完整版）
CREATE TABLE IF NOT EXISTS replays (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       game_id INTEGER NOT NULL,
                                       file_name TEXT NOT NULL,
                                       file_path TEXT NOT NULL UNIQUE,
                                       file_size INTEGER,
                                       file_modified_time INTEGER,

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

-- 用户表
CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username TEXT NOT NULL UNIQUE,
                                     email TEXT NOT NULL UNIQUE,
                                     password TEXT NOT NULL,
                                     avatar TEXT,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 邮箱验证码表
CREATE TABLE IF NOT EXISTS verification_codes (
                                                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                  email TEXT NOT NULL,
                                                  code TEXT NOT NULL,
                                                  type TEXT NOT NULL, -- register, reset_password
                                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  expires_at TIMESTAMP NOT NULL
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_verification_codes_email ON verification_codes(email);

CREATE INDEX IF NOT EXISTS idx_replays_game_difficulty ON replays(game_id, difficulty);
CREATE INDEX IF NOT EXISTS idx_replays_game_shot ON replays(game_id, shot_type);
CREATE INDEX IF NOT EXISTS idx_replays_score ON replays(game_id, difficulty, total_score DESC);