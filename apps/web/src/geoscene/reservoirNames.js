/**
 * 18 座水库名称映射
 */
export const RESERVOIR_NAME_MAP = {
  白河堡: '白河堡水库',
  半城子: '半城子水库',
  北台上: '北台上水库',
  崇青: '崇青水库',
  大宁: '大宁水库',
  大水峪: '大水峪水库',
  官厅: '官厅水库',
  海子: '海子水库',
  怀柔: '怀柔水库',
  黄松峪: '黄松峪水库',
  密云: '密云水库',
  沙厂: '沙厂水库',
  十三陵: '十三陵水库',
  桃峪口: '桃峪口水库',
  西峪: '西峪水库',
  遥桥峪: '遥桥峪水库',
  斋堂: '斋堂水库',
  珠窝: '珠窝水库'
}

/**
 * @param {string} name - 原始名称
 * @returns {string} 标准库名
 */
export function normalizeReservoirName(name) {
  if (!name) return ''
  const n = String(name).replace(/\s+/g, '').trim()
  if (!n) return ''
  if (n.endsWith('水库')) return n
  return RESERVOIR_NAME_MAP[n] || `${n}水库`
}

/**
 * @param {string} fullName - 标准库名
 * @returns {string} 简称
 */
export function toShortName(fullName) {
  return fullName ? fullName.replace('水库', '') : ''
}
