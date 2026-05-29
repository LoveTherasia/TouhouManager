// Mock 数据服务

// 东方 Project 相关图片资源（来自公开资源）
const touhouImages = {
  // 游戏封面（不同分辨率）
  gameCovers: {
    th06: {
      thumbnail: "https://upload.wikimedia.org/wikipedia/en/5/50/Touhou_Complete_Bundle_cover.jpg",
      small: "https://upload.wikimedia.org/wikipedia/en/thumb/5/50/Touhou_Complete_Bundle_cover.jpg/200px-Touhou_Complete_Bundle_cover.jpg",
      medium: "https://upload.wikimedia.org/wikipedia/en/thumb/5/50/Touhou_Complete_Bundle_cover.jpg/400px-Touhou_Complete_Bundle_cover.jpg",
      large: "https://upload.wikimedia.org/wikipedia/en/5/50/Touhou_Complete_Bundle_cover.jpg"
    }
  },
  // 角色立绘
  characters: [
    {
      name: "博丽灵梦",
      url: "https://upload.wikimedia.org/wikipedia/en/8/88/Hakurei_Reimu.png",
      width: 300,
      height: 400
    },
    {
      name: "雾雨魔理沙",
      url: "https://upload.wikimedia.org/wikipedia/en/4/4f/Kirisame_Marisa.png",
      width: 300,
      height: 400
    }
  ],
  // 风景/场景图片
  backgrounds: [
    {
      name: "博丽神社",
      url: "https://picsum.photos/seed/touhou1/1920/1080",
      width: 1920,
      height: 1080
    },
    {
      name: "魔法森林",
      url: "https://picsum.photos/seed/touhou2/1280/720",
      width: 1280,
      height: 720
    },
    {
      name: "红魔馆",
      url: "https://picsum.photos/seed/touhou3/800/600",
      width: 800,
      height: 600
    },
    {
      name: "雾之湖",
      url: "https://picsum.photos/seed/touhou4/640/480",
      width: 640,
      height: 480
    },
    {
      name: "妖怪之山",
      url: "https://picsum.photos/seed/touhou5/320/240",
      width: 320,
      height: 240
    }
  ],
  // 不同尺寸的占位图片
  placeholders: {
    avatar: {
      small: "https://picsum.photos/seed/avatar1/64/64",
      medium: "https://picsum.photos/seed/avatar1/128/128",
      large: "https://picsum.photos/seed/avatar1/256/256"
    },
    thumbnail: {
      small: "https://picsum.photos/seed/thumb1/150/150",
      medium: "https://picsum.photos/seed/thumb1/300/300",
      large: "https://picsum.photos/seed/thumb1/600/600"
    },
    banner: {
      small: "https://picsum.photos/seed/banner1/800/200",
      medium: "https://picsum.photos/seed/banner1/1200/300",
      large: "https://picsum.photos/seed/banner1/1920/480"
    },
    card: {
      small: "https://picsum.photos/seed/card1/400/300",
      medium: "https://picsum.photos/seed/card1/600/450",
      large: "https://picsum.photos/seed/card1/800/600"
    }
  }
}

const mockGames = [
  {
    id: 1,
    displayName: "东方红魔乡",
    version: "th06",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th06",
    coverImage: touhouImages.placeholders.card.medium,
    description: "东方Project第6作，首次引入符卡系统"
  },
  {
    id: 2,
    displayName: "东方妖妖梦",
    version: "th07",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th07",
    coverImage: "https://picsum.photos/seed/th07/600/450",
    description: "东方Project第7作，引入季节系统"
  },
  {
    id: 3,
    displayName: "东方永夜抄",
    version: "th08",
    installed: false,
    installPath: null,
    coverImage: "https://picsum.photos/seed/th08/600/450",
    description: "东方Project第8作，昼夜系统"
  },
  {
    id: 4,
    displayName: "东方风神录",
    version: "th10",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th10",
    coverImage: "https://picsum.photos/seed/th10/600/450",
    description: "东方Project第10作，回归原点之作"
  },
  {
    id: 5,
    displayName: "东方地灵殿",
    version: "th11",
    installed: true,
    installPath: "D:\\Games\\Touhou\\th11",
    coverImage: "https://picsum.photos/seed/th11/600/450",
    description: "东方Project第11作，地底世界"
  },
  {
    id: 6,
    displayName: "东方星莲船",
    version: "th12",
    installed: false,
    installPath: null,
    coverImage: "https://picsum.photos/seed/th12/600/450",
    description: "东方Project第12作，宇宙与佛教"
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
    thumbnail: "https://picsum.photos/seed/replay1/300/200",
    playerAvatar: touhouImages.placeholders.avatar.small,
    gameCover: touhouImages.placeholders.card.small
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
    thumbnail: "https://picsum.photos/seed/replay2/300/200",
    playerAvatar: touhouImages.placeholders.avatar.small,
    gameCover: "https://picsum.photos/seed/th07/150/150"
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
    thumbnail: "https://picsum.photos/seed/replay3/300/200",
    playerAvatar: touhouImages.placeholders.avatar.small,
    gameCover: touhouImages.placeholders.card.small
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
    thumbnail: "https://picsum.photos/seed/replay4/300/200",
    playerAvatar: touhouImages.placeholders.avatar.small,
    gameCover: "https://picsum.photos/seed/th10/150/150"
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
