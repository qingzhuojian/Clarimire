// Toast 提示
export const showToast = (title, icon = 'none', duration = 1500) => {
  uni.showToast({
    title,
    icon,
    duration,
    mask: true
  })
}

// 确认对话框
export const showConfirm = (title, content) => {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      success: (res) => {
        resolve(res.confirm)
      }
    })
  })
}

// 加载提示
export const showLoading = (title = '加载中...') => {
  uni.showLoading({
    title,
    mask: true
  })
}

// 隐藏加载
export const hideLoading = () => {
  uni.hideLoading()
}

// 页面跳转
export const navigateTo = (url) => {
  uni.navigateTo({
    url
  })
}

export const redirectTo = (url) => {
  uni.redirectTo({
    url
  })
}

export const reLaunch = (url) => {
  uni.reLaunch({
    url
  })
}

export const switchTab = (url) => {
  uni.switchTab({
    url
  })
}

// 获取页面参数
export const getPageParams = () => {
  const pages = getCurrentPages()
  if (pages.length > 0) {
    return pages[pages.length - 1].options || {}
  }
  return {}
}

// 获取用户位置
export const getLocation = () => {
  return new Promise((resolve, reject) => {
    const tryResolve = (res) => resolve(res)
    const tryReject = (err) => {
      if (typeof navigator !== 'undefined' && navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (geoRes) => {
            resolve({
              latitude: geoRes.coords.latitude,
              longitude: geoRes.coords.longitude,
            })
          },
          (geoErr) => reject(err || geoErr),
          { enableHighAccuracy: true, timeout: 15000 }
        )
      } else {
        reject(err || new Error('不支持获取位置'))
      }
    }

    if (typeof uni !== 'undefined' && uni.getLocation) {
      uni.getLocation({
        type: 'gcj02',
        geocode: true,
        success: tryResolve,
        fail: tryReject,
      })
    } else if (typeof navigator !== 'undefined' && navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (geoRes) => {
          resolve({
            latitude: geoRes.coords.latitude,
            longitude: geoRes.coords.longitude,
          })
        },
        (geoErr) => reject(geoErr || new Error('不支持获取位置')),
        { enableHighAccuracy: true, timeout: 15000 }
      )
    } else {
      reject(new Error('不支持获取位置'))
    }
  })
}

// 选择图片
export const chooseImage = (count = 1) => {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        resolve(res.tempFilePaths)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

// 拨打电话
export const makePhoneCall = (phoneNumber) => {
  uni.makePhoneCall({
    phoneNumber: String(phoneNumber)
  })
}

// 设置剪贴板
export const setClipboardData = (data) => {
  uni.setClipboardData({
    data,
    success: () => {
      showToast('已复制到剪贴板')
    }
  })
}

// 获取问候语
export const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 5) return '凌晨好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  if (hour < 22) return '晚上好'
  return '夜深了'
}

// 格式化时间
export const formatTime = (date) => {
  const d = new Date(date)
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${month}月${day}日 ${hours}:${minutes}`
}

// 相对时间
export const getRelativeTime = (date) => {
  const now = Date.now()
  const diff = now - new Date(date).getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return formatTime(date)
}

// 优先级颜色
export const getPriorityColor = (priority) => {
  switch (priority) {
    case 'urgent':
      return '#f87171'
    case 'warning':
      return '#e8b86a'
    default:
      return '#4ade80'
  }
}

// 状态颜色
export const getStatusColor = (status) => {
  switch (status) {
    case '已完成':
    case '已处理':
      return '#4ade80'
    case '进行中':
    case '处理中':
      return '#60a5fa'
    case '待处理':
    case '待核查':
      return '#94a3b8'
    default:
      return '#94a3b8'
  }
}
