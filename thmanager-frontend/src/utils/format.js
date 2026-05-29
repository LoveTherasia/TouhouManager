export function formatScore(score) {
  if (score == null) return '-'
  return Number(score).toLocaleString()
}

export function formatPlayTime(minutes) {
  if (!minutes || minutes <= 0) return '0 分钟'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) return `${hours} 小时 ${mins} 分钟`
  return `${mins} 分钟`
}

export function formatPlayTimeFromSeconds(seconds) {
  if (!seconds || seconds <= 0) return '0 分钟'
  return formatPlayTime(Math.floor(seconds / 60))
}

export function getGamePlayTimeMinutes(game) {
  if (game.playTime != null) return game.playTime
  if (game.totalPlayTimeSeconds != null) return Math.floor(game.totalPlayTimeSeconds / 60)
  return 0
}

export function getGameDisplayName(game) {
  const raw = game.titleCn || game.titleJa || game.displayName
  if (!raw) return `Game ${game.id}`
  // 兼容旧 API 返回的 "TH07 - 东方妖妖梦" 格式
  return raw.replace(/^TH\d+\s*[-–—]\s*/, '')
}

export function getGameShortName(game) {
  if (game.shortName) return game.shortName
  if (game.gameNumber) return `th${String(game.gameNumber).padStart(2, '0')}`
  return `game-${game.id}`
}

/** 封面图 URL，优先 API 字段，否则按 thXX_title 规则推断 */
export function getGameCoverUrl(game) {
  if (game.coverImage) return game.coverImage
  const sn = getGameShortName(game)
  const num = game.gameNumber ?? parseInt(sn.replace(/\D/g, ''), 10)
  const ext = [11, 15, 18, 19].includes(num) ? 'png' : 'jpg'
  return `/image/cover/${sn}_title.${ext}`
}

export function getDifficultyLabel(difficulty) {
  const map = {
    Easy: 'E', E: 'E',
    Normal: 'N', N: 'N',
    Hard: 'H', H: 'H',
    Lunatic: 'L', L: 'L',
    Extra: 'Ex', Ex: 'Ex',
    Phantasm: 'Ph', Ph: 'Ph'
  }
  return map[difficulty] || difficulty?.charAt(0) || '?'
}
