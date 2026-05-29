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
  return game.displayName || game.titleCn || game.titleJa || `Game ${game.id}`
}

export function getGameShortName(game) {
  if (game.shortName) return game.shortName
  if (game.gameNumber) return `th${String(game.gameNumber).padStart(2, '0')}`
  return `game-${game.id}`
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
