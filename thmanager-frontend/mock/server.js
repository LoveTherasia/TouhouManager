// Mock 数据服务

// 用户提供的东方 Project 相关图片（不同比例）
const touhouImages = [
  "https://api.swan416.top/api/files/pic/20260524/a9dae11afd2b4286a0803cbb0b8762c3.jpg",
  "https://api.swan416.top/api/files/pic/20260524/5323f07b1b164895bac0021b2f1df5b5.png",
  "https://api.swan416.top/api/files/pic/20260522/90cba2a21d22495b9c498247f056ecbc.jpg",
  "https://api.swan416.top/api/files/pic/20260522/9a8ad7fb45004324b808a48f63ce0107.jpg",
  "https://api.swan416.top/api/files/pic/20260509/c3f9ab40d357481eb931fbf437153330.jpg",
  "https://api.swan416.top/api/files/pic/20260509/d13c325a520d482e82cbc92f6c7a1660.jpg",
  "https://api.swan416.top/api/files/pic/20260509/15b2763089a847a78d51dfad8492531e.jpg",
  "https://api.swan416.top/api/files/pic/20260428/b0a58b620b734bd2bcb966f454a5038c.png",
  "https://api.swan416.top/api/files/pic/20260426/e1e06996338445e69ddb04479bff5132.png"
]

const mockGames = [
  {
    id: 1,
    displayName: "东方红魔乡",
    shortName: "th06",
    version: "th06",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th06",
    coverImage: touhouImages[0],
    description: "东方Project第6作，首次引入符卡系统",
    playTime: 450
  },
  {
    id: 2,
    displayName: "东方妖妖梦",
    shortName: "th07",
    version: "th07",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th07",
    coverImage: touhouImages[1],
    description: "东方Project第7作，引入季节系统",
    playTime: 320
  },
  {
    id: 3,
    displayName: "东方永夜抄",
    shortName: "th08",
    version: "th08",
    installed: false,
    installPath: null,
    coverImage: touhouImages[2],
    description: "东方Project第8作，昼夜系统",
    playTime: 0
  },
  {
    id: 4,
    displayName: "东方风神录",
    shortName: "th10",
    version: "th10",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th10",
    coverImage: touhouImages[3],
    description: "东方Project第10作，回归原点之作",
    playTime: 280
  },
  {
    id: 5,
    displayName: "东方地灵殿",
    shortName: "th11",
    version: "th11",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th11",
    coverImage: touhouImages[4],
    description: "东方Project第11作，地底世界",
    playTime: 200
  },
  {
    id: 6,
    displayName: "东方星莲船",
    shortName: "th12",
    version: "th12",
    installed: false,
    installPath: null,
    coverImage: touhouImages[5],
    description: "东方Project第12作，宇宙与佛教",
    playTime: 0
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
    timestamp: "2024-01-01T12:00:00",
    thumbnail: touhouImages[6]
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
    timestamp: "2024-01-02T15:30:00",
    thumbnail: touhouImages[7]
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
    timestamp: "2024-01-03T09:15:00",
    thumbnail: touhouImages[8]
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
    timestamp: "2024-01-04T18:45:00",
    thumbnail: touhouImages[0]
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
  gameStatistics: mockGameStatistics,
  images: touhouImages
}
