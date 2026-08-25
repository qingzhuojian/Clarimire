export const taskStatusLabel = (s) =>
  ({
    pending: '待处理',
    assigned: '已指派',
    in_progress: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }[s] || s)

export const issueStatusLabel = (s) =>
  ({
    pending: '待审核',
    reviewing: '审核中',
    assigned: '已指派',
    resolved: '已解决',
    closed: '已关闭'
  }[s] || s)

export const zoneLabel = (z) =>
  ({
    core: '核心区',
    buffer: '缓冲圈',
    remote: '异地'
  }[z] || z)

export const zoneLabelShort = (z) =>
  ({
    core: '核心',
    buffer: '缓冲',
    remote: '异地'
  }[z] || z)

export const formatTime = (value) => {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export const isToday = (value) => {
  if (!value) return false
  const d = new Date(value)
  const now = new Date()
  return (
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate()
  )
}

export const greetingText = () => {
  const h = new Date().getHours()
  if (h < 12) return '早上好'
  if (h < 18) return '下午好'
  return '晚上好'
}

export const taskTypeLabel = (t) =>
  ({
    daily: '日常',
    assigned: '指派',
    emergency: '突发'
  }[t] || t || '-')

export const issueCategoryLabel = (c) =>
  ({
    water_quality: '水质异常',
    facility: '设施损坏',
    pollution: '污染排放',
    other: '其他'
  }[c] || c || '其他')

export const parsePhotos = (photos) => {
  if (!photos) return []
  if (Array.isArray(photos)) return photos
  try {
    const parsed = JSON.parse(photos)
    return Array.isArray(parsed) ? parsed : [String(photos)]
  } catch {
    return String(photos)
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean)
  }
}

export const photoUrl = (url) => {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  return url.startsWith('/') ? url : `/${url}`
}
