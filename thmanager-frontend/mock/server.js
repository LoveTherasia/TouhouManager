// Mock 数据服务

function coverFor(shortName) {
  const pngVersions = new Set(['th11', 'th15', 'th18', 'th19'])
  const ext = pngVersions.has(shortName) ? 'png' : 'jpg'
  return `/image/cover/${shortName}_title.${ext}`
}

const mockGames = [
  {
    id: 1,
    displayName: "东方红魔乡",
    shortName: "th06",
    version: "th06",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th06",
    coverImage: coverFor('th06'),
    description: "东方 Project 第 6 作，首次引入符卡系统",
    playTime: 450
  },
  {
    id: 2,
    displayName: "东方妖妖梦",
    shortName: "th07",
    version: "th07",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th07",
    coverImage: coverFor('th07'),
    description: "东方 Project 第 7 作，引入季节系统",
    playTime: 320
  },
  {
    id: 3,
    displayName: "东方永夜抄",
    shortName: "th08",
    version: "th08",
    installed: false,
    installPath: null,
    coverImage: coverFor('th08'),
    description: "东方 Project 第 8 作，昼夜系统",
    playTime: 0
  },
  {
    id: 4,
    displayName: "东方风神录",
    shortName: "th10",
    version: "th10",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th10",
    coverImage: coverFor('th10'),
    description: "东方 Project 第 10 作，回归原点之作",
    playTime: 280
  },
  {
    id: 5,
    displayName: "东方地灵殿",
    shortName: "th11",
    version: "th11",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th11",
    coverImage: coverFor('th11'),
    description: "东方 Project 第 11 作，地底世界",
    playTime: 200
  },
  {
    id: 6,
    displayName: "东方星莲船",
    shortName: "th12",
    version: "th12",
    installed: false,
    installPath: null,
    coverImage: coverFor('th12'),
    description: "东方 Project 第 12 作，宇宙与佛教",
    playTime: 0
  }
]

const mockReplays = [
  {
    id: 1,
    gameId: 1,
    fileName: "th06_20240101_001.rpy",
    playerName: "Reimu",
    character: "博丽灵梦",
    stage: "Stage 6",
    reachedStageNumber: 6,
    totalScore: 12500000,
    difficulty: "Lunatic",
    difficultyDisplay: "L",
    cleared: true,
    gameDate: "2024-01-01T12:00:00",
    totalFrames: 1620000
  },
  {
    id: 2,
    gameId: 2,
    fileName: "th07_20240102_001.rpy",
    playerName: "Marisa",
    character: "雾雨魔理沙",
    stage: "Stage 5",
    reachedStageNumber: 5,
    totalScore: 8900000,
    difficulty: "Hard",
    difficultyDisplay: "H",
    cleared: false,
    gameDate: "2024-01-02T15:30:00",
    totalFrames: 1152000
  },
  {
    id: 3,
    gameId: 1,
    fileName: "th06_20240103_001.rpy",
    playerName: "Reimu",
    character: "博丽灵梦",
    stage: "Stage 4",
    reachedStageNumber: 4,
    totalScore: 5600000,
    difficulty: "Normal",
    difficultyDisplay: "N",
    cleared: false,
    gameDate: "2024-01-03T09:15:00",
    totalFrames: 720000
  },
  {
    id: 4,
    gameId: 4,
    fileName: "th10_20240104_001.rpy",
    playerName: "Reimu",
    character: "博丽灵梦",
    stage: "Stage 6",
    reachedStageNumber: 6,
    totalScore: 15200000,
    difficulty: "Lunatic",
    difficultyDisplay: "L",
    cleared: true,
    gameDate: "2024-01-04T18:45:00",
    totalFrames: 1980000
  },
  {
    id: 5,
    gameId: 5,
    fileName: "th11_20240105_001.rpy",
    playerName: "Marisa",
    character: "雾雨魔理沙",
    stage: "Stage 3",
    reachedStageNumber: 3,
    totalScore: 3200000,
    difficulty: "Normal",
    difficultyDisplay: "N",
    cleared: false,
    gameDate: "2024-01-05T20:00:00",
    totalFrames: 540000
  },
  {
    id: 6,
    gameId: 2,
    fileName: "th07_20240106_001.rpy",
    playerName: "Sakuya",
    character: "十六夜咲夜",
    stage: "Stage 6",
    reachedStageNumber: 6,
    totalScore: 9800000,
    difficulty: "Lunatic",
    difficultyDisplay: "L",
    cleared: true,
    gameDate: "2024-01-06T14:20:00",
    totalFrames: 1440000
  }
]

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
  images: touhouImages
}
