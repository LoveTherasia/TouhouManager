// Mock 数据服务
const mockGames = [
  {
    id: 1,
    displayName: "东方红魔乡",
    version: "th06",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th06"
  },
  {
    id: 2,
    displayName: "东方妖妖梦",
    version: "th07",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th07"
  },
  {
    id: 3,
    displayName: "东方永夜抄",
    version: "th08",
    installed: false,
    installPath: null
  },
  {
    id: 4,
    displayName: "东方风神录",
    version: "th10",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th10"
  },
  {
    id: 5,
    displayName: "东方地灵殿",
    version: "th11",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th11"
  },
  {
    id: 6,
    displayName: "东方星莲船",
    version: "th12",
    installed: false,
    installPath: null
  }
]

const mockReplays = [
  {
    id: 1,
    gameId: 1,
    gameName: "东方红魔乡",
    filename: "th06_20240101_001.rpy",
    playerName: "Reimu",
    stage: "Stage 6",
    score: 12500000,
    difficulty: "Lunatic",
    timestamp: "2024-01-01T12:00:00"
  },
  {
    id: 2,
    gameId: 2,
    gameName: "东方妖妖梦",
    filename: "th07_20240102_001.rpy",
    playerName: "Marisa",
    stage: "Stage 5",
    score: 8900000,
    difficulty: "Hard",
    timestamp: "2024-01-02T15:30:00"
  },
  {
    id: 3,
    gameId: 1,
    gameName: "东方红魔乡",
    filename: "th06_20240103_001.rpy",
    playerName: "Reimu",
    stage: "Stage 4",
    score: 5600000,
    difficulty: "Normal",
    timestamp: "2024-01-03T09:15:00"
  },
  {
    id: 4,
    gameId: 4,
    gameName: "东方风神录",
    filename: "th10_20240104_001.rpy",
    playerName: "Reimu",
    stage: "Stage 6",
    score: 15200000,
    difficulty: "Lunatic",
    timestamp: "2024-01-04T18:45:00"
  }
]

const mockStatistics = {
  totalGames: 6,
  installedGames: 4,
  totalReplays: 4,
  totalPlayTime: 1250,
  favoriteGame: "东方红魔乡"
}

const mockGameStatistics = {
  gameId: 1,
  gameName: "东方红魔乡",
  totalPlayTime: 450,
  totalReplays: 2,
  bestScore: 12500000,
  averageScore: 9050000,
  mostPlayedDifficulty: "Lunatic",
  completionRate: 85
}

// 导出 mock 数据
export const mockData = {
  games: mockGames,
  replays: mockReplays,
  statistics: mockStatistics,
  gameStatistics: mockGameStatistics
}
