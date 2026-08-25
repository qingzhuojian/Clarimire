<template>
  <div>
    <div class="form-block">
      <div class="form-block__title">01 基本信息</div>
      <label class="label">{{ titleLabel }} <span class="required">*</span></label>
      <input v-model="model.title" class="input" :placeholder="titlePlaceholder" />

      <label class="label">问题类型</label>
      <div class="chip-row">
        <button
          v-for="c in categories"
          :key="c.value"
          type="button"
          class="chip"
          :class="{ 'chip--active': model.category === c.value }"
          @click="model.category = c.value"
        >{{ c.label }}</button>
      </div>
    </div>

    <div class="form-block">
      <div class="form-block__title">02 位置信息</div>
      <label class="label">相关水库</label>
      <ReservoirSearchSelect
        v-if="reservoirs.length"
        v-model="model.reservoirName"
        :options="reservoirs"
        placeholder="输入关键字搜索（可选）"
      />
      <input v-else v-model="model.reservoirName" class="input" placeholder="可选，如：密云水库" />

      <label class="label">现场定位</label>
      <div class="location-box">
        <div class="location-box__coords">
          {{ model.lat != null ? `${model.lat}, ${model.lng}` : '尚未获取定位' }}
        </div>
        <p v-if="isDev" class="location-box__hint">开发环境可用右下角 DEV 面板模拟坐标</p>
        <button type="button" class="btn btn-secondary btn-sm" style="margin-top:10px;width:auto" @click="locate">
          获取定位
        </button>
      </div>
    </div>

    <div class="form-block">
      <div class="form-block__title">03 详细描述</div>
      <label class="label">情况描述 <span class="required">*</span></label>
      <textarea v-model="model.description" class="textarea" :placeholder="descPlaceholder" />
    </div>

    <div class="form-block">
      <div class="form-block__title">04 现场照片（可选）</div>
      <div class="photo-row">
        <label v-if="photoList.length < 3" class="photo-add">
          <input type="file" accept="image/*" hidden @change="onPick" />
          <span>+</span>
        </label>
        <div v-for="(p, i) in photoList" :key="p + i" class="photo-item">
          <img :src="toPhotoUrl(p)" alt="" />
          <button type="button" class="photo-item__del" @click="removePhoto(i)">×</button>
        </div>
      </div>
      <p class="hint">最多 3 张，单张不超过 5MB</p>
    </div>

    <button
      v-if="!dualActions"
      class="btn btn-primary"
      :disabled="submitting || uploading"
      @click="$emit('submit')"
    >
      {{ uploading ? '上传中...' : submitting ? '提交中...' : submitText }}
    </button>
    <div v-else class="dual-actions">
      <button
        class="btn btn-primary"
        :disabled="submitting || uploading"
        @click="$emit('onsite')"
      >当场处理</button>
      <button
        class="btn btn-secondary"
        :disabled="submitting || uploading"
        @click="$emit('escalate')"
      >上报管理员</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { systemAPI, uploadAPI } from '@/api'
import { useToast } from '@/composables/useToast'
import { photoUrl } from '@/utils/labels'
import ReservoirSearchSelect from '@/components/ui/ReservoirSearchSelect.vue'
const toPhotoUrl = photoUrl

const props = defineProps({
  model: { type: Object, required: true },
  submitting: { type: Boolean, default: false },
  dualActions: { type: Boolean, default: false },
  titleLabel: { type: String, default: '问题标题' },
  titlePlaceholder: { type: String, default: '请简要描述问题' },
  descPlaceholder: { type: String, default: '请详细描述情况' },
  submitText: { type: String, default: '提交上报' }
})
defineEmits(['submit', 'onsite', 'escalate'])

const { show: showToast } = useToast()
const reservoirs = ref([])
const uploading = ref(false)
const isDev = import.meta.env.DEV

const categories = [
  { label: '水质异常', value: 'water_quality' },
  { label: '设施损坏', value: 'facility' },
  { label: '污染排放', value: 'pollution' },
  { label: '其他', value: 'other' }
]

const photoList = computed(() => {
  if (!props.model.photos) return []
  try {
    const p = JSON.parse(props.model.photos)
    return Array.isArray(p) ? p : []
  } catch {
    return props.model.photos ? [props.model.photos] : []
  }
})

const setPhotos = (arr) => {
  props.model.photos = arr.length ? JSON.stringify(arr) : ''
}

const applyCoords = (lat, lng) => {
  props.model.lat = Number(Number(lat).toFixed(6))
  props.model.lng = Number(Number(lng).toFixed(6))
}

const readDevLocation = () => {
  const lat = localStorage.getItem('devLat')
  const lng = localStorage.getItem('devLng')
  if (lat && lng) {
    applyCoords(lat, lng)
    return true
  }
  return false
}

const onDevLocation = (e) => {
  if (e?.detail?.lat == null || e?.detail?.lng == null) return
  applyCoords(e.detail.lat, e.detail.lng)
  showToast('已使用 DEV 模拟坐标', 'success')
}

const locate = () => {
  if (isDev && readDevLocation()) {
    showToast('已读取 DEV 模拟坐标', 'info')
    return
  }
  if (!navigator.geolocation) {
    if (isDev) {
      applyCoords(40.485, 116.845)
      showToast('浏览器不支持定位，已使用默认坐标', 'info')
      return
    }
    showToast('浏览器不支持定位', 'error')
    return
  }
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      applyCoords(pos.coords.latitude, pos.coords.longitude)
      showToast('定位成功', 'success')
    },
    () => {
      if (isDev) {
        applyCoords(40.485, 116.845)
        showToast('定位失败，已使用默认坐标（可用右下角 DEV）', 'info')
        return
      }
      showToast('定位失败，请检查权限', 'error')
    }
  )
}

const onPick = async (e) => {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    showToast('图片不能超过 5MB', 'error')
    return
  }
  uploading.value = true
  try {
    const res = await uploadAPI.photo(file)
    const url = res.data?.url
    if (!url) throw new Error('上传失败')
    setPhotos([...photoList.value, url])
    showToast('上传成功', 'success')
  } catch (err) {
    showToast(err.message || '上传失败', 'error')
  } finally {
    uploading.value = false
  }
}

const removePhoto = (i) => {
  const next = [...photoList.value]
  next.splice(i, 1)
  setPhotos(next)
}

onMounted(async () => {
  if (!props.model.category) props.model.category = 'other'
  if (isDev) {
    readDevLocation()
    window.addEventListener('dev-location', onDevLocation)
  }
  try {
    const res = await systemAPI.getReservoirLocations()
    reservoirs.value = res.data || []
  } catch {
    reservoirs.value = []
  }
})

onUnmounted(() => {
  if (isDev) window.removeEventListener('dev-location', onDevLocation)
})
</script>

<style scoped>
.form-block {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}
.form-block:last-of-type { border-bottom: none; }
.form-block__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
}
.required { color: var(--color-danger); }
.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.chip {
  border: 1px solid var(--color-border);
  background: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
}
.chip--active {
  background: var(--color-primary-light);
  border-color: var(--color-primary);
  color: var(--color-primary);
  font-weight: 600;
}
.photo-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.photo-add {
  width: 72px;
  height: 72px;
  border: 1px dashed var(--color-border);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: var(--color-text-muted);
  cursor: pointer;
  background: var(--color-bg-muted);
}
.photo-item {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
}
.photo-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.photo-item__del {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 50%;
  background: rgba(0,0,0,0.55);
  color: #fff;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
}
.hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-muted);
}
.dual-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
