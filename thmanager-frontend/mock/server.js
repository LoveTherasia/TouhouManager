// Mock 数据服务

// 东方整数作基本信息（TH06-TH19，共14作）
const touhouGames = [
  { id: 1,  gameNumber: 6,  titleJa: '東方紅魔郷',       titleCn: '东方红魔乡',   titleEn: 'EoSD',    exeName: 'th06.exe', replayFolder: 'replay' },
  { id: 2,  gameNumber: 7,  titleJa: '東方妖々夢',       titleCn: '东方妖妖梦',   titleEn: 'PCB',     exeName: 'th07.exe', replayFolder: 'replay' },
  { id: 3,  gameNumber: 8,  titleJa: '東方永夜抄',       titleCn: '东方永夜抄',   titleEn: 'IN',      exeName: 'th08.exe', replayFolder: 'replay' },
  { id: 4,  gameNumber: 9,  titleJa: '東方花映塚',       titleCn: '东方花映冢',   titleEn: 'PoFV',    exeName: 'th09.exe', replayFolder: 'replay' },
  { id: 5,  gameNumber: 10, titleJa: '東方風神録',       titleCn: '东方风神录',   titleEn: 'MoF',     exeName: 'th10.exe', replayFolder: 'replay' },
  { id: 6,  gameNumber: 11, titleJa: '東方地霊殿',       titleCn: '东方地灵殿',   titleEn: 'SA',      exeName: 'th11.exe', replayFolder: 'replay' },
  { id: 7,  gameNumber: 12, titleJa: '東方星蓮船',       titleCn: '东方星莲船',   titleEn: 'UFO',     exeName: 'th12.exe', replayFolder: 'replay' },
  { id: 8,  gameNumber: 13, titleJa: '東方神霊廟',       titleCn: '东方神灵庙',   titleEn: 'TD',      exeName: 'th13.exe', replayFolder: 'replay' },
  { id: 9,  gameNumber: 14, titleJa: '東方輝針城',       titleCn: '东方辉针城',   titleEn: 'DDC',     exeName: 'th14.exe', replayFolder: 'replay' },
  { id: 10, gameNumber: 15, titleJa: '東方紺珠伝',       titleCn: '东方绀珠传',   titleEn: 'LoLK',    exeName: 'th15.exe', replayFolder: 'replay' },
  { id: 11, gameNumber: 16, titleJa: '東方天空璋',       titleCn: '东方天空璋',   titleEn: 'HSiFS',   exeName: 'th16.exe', replayFolder: 'replay' },
  { id: 12, gameNumber: 17, titleJa: '東方鬼形獣',       titleCn: '东方鬼形兽',   titleEn: 'WBaWC',   exeName: 'th17.exe', replayFolder: 'replay' },
  { id: 13, gameNumber: 18, titleJa: '東方虹龍洞',       titleCn: '东方虹龙洞',   titleEn: 'UM',      exeName: 'th18.exe', replayFolder: 'replay' },
  { id: 14, gameNumber: 19, titleJa: '東方獣王園',       titleCn: '东方兽王园',   titleEn: 'UDoALG',  exeName: 'th19.exe', replayFolder: 'replay' }
];

const installedPaths = [
  null, 'D:\\Games\\Touhou\\th07', null, null, 'D:\\Games\\Touhou\\th10', 'D:\\Games\\Touhou\\th11', null, null, null,
  null, null, null, null, null
];

const mockGames = touhouGames.map((g, i) => ({
  id: g.id,
  gameNumber: g.gameNumber,
  displayName: g.titleCn,
  shortName: `th${String(g.gameNumber).padStart(2, '0')}`,
  version: `th${String(g.gameNumber).padStart(2, '0')}`,
  installed: installedPaths[i] !== null,
  installPath: installedPaths[i],
  coverImage: `/image/cover/th${String(g.gameNumber).padStart(2, '0')}_title.jpg`,
  description: getGameDesc(g.gameNumber),
  playTime: getPlayTime(g.gameNumber),
  titleJa: g.titleJa,
  titleCn: g.titleCn,
  titleEn: g.titleEn,
  exeName: g.exeName,
  replayFolder: g.replayFolder
}));

function getGameDesc(num) {
  const descs = {
    6:  '博丽灵梦调查红魔馆的异变，面对吸血鬼姐妹蕾米莉亚和芙兰朵露，在夏日幻想乡展开弹幕对决',
    7:  '春雪异变笼罩幻想乡，灵梦与魔理沙深入冥界，揭开西行寺幽幽子与八云紫背后的秘密',
    8:  '不老的不夜城，永远亭的公主与月之使者卷入永恒之夜，众角色在迷途竹林中探索真相',
    9:  '幻想乡被大量花朵覆盖，亡灵与妖怪争夺樱花树下的春度，在生死交错的弹幕中展开对决',
    10: '山上的神社搬来外界神明，守矢一族与八坂神奈子试图收集信仰，引发幻想乡的信仰之争',
    11: '地底灼热地狱中怨灵暴走，古明地觉与古明地恋姐妹之间的情感纠葛在地下世界引发异变',
    12: '神奇的宝船出现在天空之上，揭秘被封印的魔法与僧侣，在云层之上展开神秘旅程',
    13: '三角圣域中的仙人复归，道士宫古芳香与霍青娥展开的复活亡灵计划导致大量魂魄彷徨',
    14: '小人族企图利用万宝槌的力量向人类复仇，在无数弹幕的逃亡与战斗中逐渐揭开真相',
    15: '月之都的入侵计划展开，纯狐与月之民之间的千年恩怨在永恒的战斗中被揭露与解明',
    16: '四季异变使幻想乡陷入混乱，三位妖精与隐岐奈展开抗争，自然之力的冲突与调和',
    17: '畜生界发生剧烈动荡，动物灵与埴轮灵的对抗引发地狱变化，信仰与灵魂的较量',
    18: '神秘卡片在幻想乡流通，大天狗饭纲丸龙策划的阴谋在虹龙洞展开，收藏与战斗交织',
    19: '猿田彦神计划征服幻想乡，召唤各类野兽灵魂进行驯服，展开全新的弹幕之战'
  };
  return descs[num] || '东方 Project 系列作品';
}

function getPlayTime(num) {
  const times = { 6: 450, 7: 320, 10: 280, 11: 200, 14: 150, 16: 90, 18: 60 };
  return times[num] || 0;
}

const mockReplays = [
  { id: 1, gameId: 1,  fileName: 'th06_20240101_001.rpy', playerName: 'Reimu', character: '博丽灵梦', stage: 'Stage 6', reachedStageNumber: 6, totalScore: 12500000, difficulty: 'Lunatic', difficultyDisplay: 'L', cleared: true, gameDate: '2024-01-01T12:00:00', totalFrames: 1620000 },
  { id: 2, gameId: 1,  fileName: 'th06_20240215_001.rpy', playerName: 'Marisa', character: '雾雨魔理沙', stage: 'Stage 6', reachedStageNumber: 6, totalScore: 9800000, difficulty: 'Hard', difficultyDisplay: 'H', cleared: true, gameDate: '2024-02-15T14:30:00', totalFrames: 1450000 },
  { id: 3, gameId: 1,  fileName: 'th06_20240310_001.rpy', playerName: 'Reimu', character: '博丽灵梦', stage: 'Stage 4', reachedStageNumber: 4, totalScore: 5600000, difficulty: 'Normal', difficultyDisplay: 'N', cleared: false, gameDate: '2024-03-10T09:15:00', totalFrames: 720000 },
  { id: 4, gameId: 2,  fileName: 'th07_20240102_001.rpy', playerName: 'Marisa', character: '雾雨魔理沙', stage: 'Stage 5', reachedStageNumber: 5, totalScore: 8900000, difficulty: 'Hard', difficultyDisplay: 'H', cleared: false, gameDate: '2024-01-02T15:30:00', totalFrames: 1152000 },
  { id: 5, gameId: 2,  fileName: 'th07_20240106_001.rpy', playerName: 'Sakuya', character: '十六夜咲夜', stage: 'Stage 6', reachedStageNumber: 6, totalScore: 9800000, difficulty: 'Lunatic', difficultyDisplay: 'L', cleared: true, gameDate: '2024-01-06T14:20:00', totalFrames: 1440000 },
  { id: 6, gameId: 2,  fileName: 'th07_20240220_001.rpy', playerName: 'Reimu', character: '博丽灵梦', stage: 'Stage 6', reachedStageNumber: 6, totalScore: 11200000, difficulty: 'Lunatic', difficultyDisplay: 'L', cleared: true, gameDate: '2024-02-20T18:00:00', totalFrames: 1580000 },
  { id: 7, gameId: 5,  fileName: 'th10_20240104_001.rpy', playerName: 'Reimu', character: '博丽灵梦', stage: 'Stage 6', reachedStageNumber: 6, totalScore: 15200000, difficulty: 'Lunatic', difficultyDisplay: 'L', cleared: true, gameDate: '2024-01-04T18:45:00', totalFrames: 1980000 },
  { id: 8, gameId: 5,  fileName: 'th10_20240315_001.rpy', playerName: 'Marisa', character: '雾雨魔理沙', stage: 'Stage 5', reachedStageNumber: 5, totalScore: 10500000, difficulty: 'Lunatic', difficultyDisplay: 'L', cleared: false, gameDate: '2024-03-15T20:00:00', totalFrames: 1700000 },
  { id: 9, gameId: 6,  fileName: 'th11_20240105_001.rpy', playerName: 'Marisa', character: '雾雨魔理沙', stage: 'Stage 3', reachedStageNumber: 3, totalScore: 3200000, difficulty: 'Normal', difficultyDisplay: 'N', cleared: false, gameDate: '2024-01-05T20:00:00', totalFrames: 540000 },
  { id: 10, gameId: 6,  fileName: 'th11_20240210_001.rpy', playerName: 'Aya', character: '射命丸文', stage: 'Stage 6', reachedStageNumber: 6, totalScore: 8700000, difficulty: 'Lunatic', difficultyDisplay: 'L', cleared: true, gameDate: '2024-02-10T21:30:00', totalFrames: 1400000 },
  { id: 11, gameId: 9,  fileName: 'th14_20240401_001.rpy', playerName: 'Meiling', character: '芙兰朵露·斯卡雷特', stage: 'Stage 6', reachedStageNumber: 6, totalScore: 13800000, difficulty: 'Lunatic', difficultyDisplay: 'L', cleared: true, gameDate: '2024-04-01T16:00:00', totalFrames: 1850000 },
  { id: 12, gameId: 9,  fileName: 'th14_20240520_001.rpy', playerName: 'Koishi', character: '古明地恋', stage: 'Stage 5', reachedStageNumber: 5, totalScore: 10200000, difficulty: 'Lunatic', difficultyDisplay: 'L', cleared: false, gameDate: '2024-05-20T19:00:00', totalFrames: 1600000 },
  { id: 13, gameId: 11, fileName: 'th16_20240610_001.rpy', playerName: 'Reimu', character: '博丽灵梦', stage: 'Stage 6', reachedStageNumber: 6, totalScore: 14100000, difficulty: 'Lunatic', difficultyDisplay: 'L', cleared: true, gameDate: '2024-06-10T17:30:00', totalFrames: 1900000 },
  { id: 14, gameId: 11, fileName: 'th16_20240815_001.rpy', playerName: 'Sanae', character: '穣子', stage: 'Stage 4', reachedStageNumber: 4, totalScore: 7500000, difficulty: 'Hard', difficultyDisplay: 'H', cleared: false, gameDate: '2024-08-15T13:00:00', totalFrames: 980000 },
  { id: 15, gameId: 13, fileName: 'th18_20240920_001.rpy', playerName: 'Marisa', character: '雾雨魔理沙', stage: 'Stage 5', reachedStageNumber: 5, totalScore: 9200000, difficulty: 'Hard', difficultyDisplay: 'H', cleared: false, gameDate: '2024-09-20T20:30:00', totalFrames: 1250000 },
  { id: 16, gameId: 14, fileName: 'th19_20241001_001.rpy', playerName: 'Reimu', character: '博丽灵梦', stage: 'Stage 4', reachedStageNumber: 4, totalScore: 4800000, difficulty: 'Normal', difficultyDisplay: 'N', cleared: false, gameDate: '2024-10-01T15:00:00', totalFrames: 650000 },
];

const mockStatistics = {
  totalGames: mockGames.length,
  totalReplays: mockReplays.length,
  totalPlayTime: mockGames.reduce((s, g) => s + (g.playTime || 0), 0),
  clearedCount: mockReplays.filter(r => r.cleared).length
}

const mockPlayTimeStats = mockGames
  .filter(g => g.playTime > 0)
  .map(g => ({
    gameId: g.id,
    gameName: g.displayName,
    playTime: g.playTime,
    replayCount: mockReplays.filter(r => r.gameId === g.id).length
  }))
  .sort((a, b) => b.playTime - a.playTime)

const mockScoreStats = [...mockReplays]
  .sort((a, b) => b.totalScore - a.totalScore)
  .slice(0, 20)
  .map(r => ({
    id: r.id,
    gameId: r.gameId,
    gameTitle: mockGames.find(g => g.id === r.gameId)?.displayName,
    playerName: r.playerName,
    difficulty: r.difficulty,
    totalScore: r.totalScore,
    gameDate: r.gameDate
  }))

export const mockData = {
  games: mockGames,
  replays: mockReplays,
  statistics: mockStatistics,
  playTimeStats: mockPlayTimeStats,
  scoreStats: mockScoreStats,
  images: []
}
