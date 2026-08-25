/** 分级渲染颜色方案（1-6 级） */
export const COLOR_SCHEMES = {
  1: ['#ff4d4f'],
  2: ['#ff4d4f', '#52c41a'],
  3: ['#ff4d4f', '#ffd700', '#52c41a'],
  4: ['#ff4d4f', '#ff7a45', '#ffd700', '#52c41a'],
  5: ['#ff4d4f', '#ff7a45', '#ffd700', '#73d13d', '#52c41a'],
  6: ['#ff4d4f', '#ff7a45', '#ffd700', '#bae637', '#73d13d', '#52c41a']
}

/**
 * 等量分级（与 Aqua MapEditor 一致）
 * @param {number[]} data
 * @param {number} numClasses
 */
export function equalCountClassification(data, numClasses) {
  if (!data.length || numClasses <= 0) return []
  const sortedData = [...data].sort((a, b) => a - b)
  const scheme = COLOR_SCHEMES[Math.min(numClasses, 6)] || COLOR_SCHEMES[3]
  const perClass = Math.ceil(sortedData.length / numClasses)
  const result = []

  for (let i = 0; i < numClasses; i++) {
    const startIndex = i * perClass
    if (startIndex >= sortedData.length) break
    const endIndex = i === numClasses - 1 ? sortedData.length : Math.min(sortedData.length, (i + 1) * perClass)
    result.push({
      min: parseFloat(sortedData[startIndex].toFixed(2)),
      max: parseFloat(sortedData[endIndex - 1].toFixed(2)),
      color: scheme[i] || '#999999'
    })
  }
  return result
}
