<template>
  <AppShell title="问题跟进" show-back :show-tab="false" :show-logout="false">
    <div v-if="loading" class="loading-tip">加载中...</div>
    <div v-else-if="task" class="followup">
      <!-- 任务信息（对齐旧 IssueTracking） -->
      <section class="panel">
        <div class="panel__head">
          <h3 class="panel__title">任务信息</h3>
          <span class="status-badge" :class="`status-badge--${task.status}`">{{ statusLabel(task.status) }}</span>
          <span v-if="task.taskType" class="type-chip" :class="`type-chip--${task.taskType}`">
            {{ typeLabel(task.taskType) }}
          </span>
        </div>
        <h2 class="followup__name">{{ task.title }}</h2>
        <div class="info-list">
          <div class="info-row">
            <span class="info-row__label">水库名称</span>
            <span class="info-row__value">{{ task.reservoirName || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-row__label">截止时间</span>
            <span class="info-row__value">{{ formatTime(task.dueTime) }}</span>
          </div>
          <div class="info-row">
            <span class="info-row__label">指派对象</span>
            <span class="info-row__value">{{ task.assigneeName || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-row__label">创建时间</span>
            <span class="info-row__value">{{ formatTime(task.createTime) }}</span>
          </div>
          <div v-if="task.createdBy" class="info-row">
            <span class="info-row__label">创建人</span>
            <span class="info-row__value">{{ task.createdBy }}</span>
          </div>
        </div>
        <div v-if="task.description" class="note-box">
          <div class="note-box__label">派发说明 / 管理员备注</div>
          <p class="note-box__text">{{ task.description }}</p>
        </div>
      </section>

      <!-- 现场处理情况 -->
      <section class="panel">
        <h3 class="panel__title panel__title--solo">现场处理情况</h3>

        <div class="field">
          <label class="field__label">现场情况描述 <span class="req">*</span></label>
          <textarea
            v-model="situation"
            class="textarea"
            rows="6"
            placeholder="请详细描述现场处理情况、采取的措施等…"
          />
          <p v-if="errors.situation" class="field__error">{{ errors.situation }}</p>
        </div>

        <div class="field">
          <label class="field__label">处理结果 <span class="req">*</span></label>
          <div class="result-grid">
            <button
              v-for="opt in resultOptions"
              :key="opt.value"
              type="button"
              class="result-chip"
              :class="{ 'result-chip--active': result === opt.value, [`result-chip--${opt.tone}`]: true }"
              @click="result = opt.value"
            >
              <span class="result-chip__title">{{ opt.label }}</span>
              <span class="result-chip__desc">{{ opt.desc }}</span>
            </button>
          </div>
          <p v-if="errors.result" class="field__error">{{ errors.result }}</p>
        </div>

        <div class="field">
          <label class="field__label">现场照片（可选）</label>
          <div class="photo-row">
            <label v-if="photos.length < 3" class="photo-add">
              <input type="file" accept="image/*" hidden :disabled="uploading" @change="onPick" />
              <span>{{ uploading ? '…' : '+' }}</span>
            </label>
            <div v-for="(p, i) in photos" :key="p + i" class="photo-item">
              <img :src="toPhotoUrl(p)" alt="" />
              <button type="button" class="photo-item__del" @click="removePhoto(i)">×</button>
            </div>
          </div>
          <p class="field__hint">最多 3 张，单张不超过 5MB</p>
        </div>

        <div class="field">
          <label class="field__label">补充备注</label>
          <textarea
            v-model="remark"
            class="textarea"
            rows="3"
            placeholder="可选：需管理员知晓的额外说明"
          />
        </div>
      </section>

      <div class="followup__actions">
        <button
          class="btn btn-primary"
          :disabled="submitting || uploading"
          @click="submit"
        >
          {{ submitting ? '提交中...' : '标记已处理并完成任务' }}
        </button>
        <p class="followup__tip">提交前请确认已完成到场打卡；选择「需要继续跟进」时问题将重新进入待处理。</p>
      </div>
    </div>
    <EmptyState v-else text="任务不存在" icon="任" action-text="返回" @action="$router.back()" />
  </AppShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { patrolAPI, issueAPI, uploadAPI } from '@/api'
import { useToast } from '@/composables/useToast'
import {
  taskStatusLabel as statusLabel,
  taskTypeLabel as typeLabel,
  formatTime,
  photoUrl
} from '@/utils/labels'
import AppShell from '@/components/layout/AppShell.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const toPhotoUrl = photoUrl
const route = useRoute()
const router = useRouter()
const { show: showToast } = useToast()

const task = ref(null)
const loading = ref(true)
const situation = ref('')
const result = ref('')
const remark = ref('')
const photos = ref([])
const submitting = ref(false)
const uploading = ref(false)
const errors = ref({ situation: '', result: '' })

const resultOptions = [
  { value: 'resolved', label: '问题已解决', desc: '现场已处置完毕', tone: 'ok' },
  { value: 'improved', label: '情况已改善', desc: '风险已明显下降', tone: 'ok' },
  { value: 'ongoing', label: '需要继续跟进', desc: '仍需管理员关注', tone: 'warn' }
]

const resultText = Object.fromEntries(resultOptions.map((o) => [o.value, o.label]))

onMounted(async () => {
  loading.value = true
  try {
    const res = await patrolAPI.getTask(route.params.id)
    task.value = res.data
  } catch {
    task.value = null
  } finally {
    loading.value = false
  }
})

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
    photos.value = [...photos.value, url]
    showToast('上传成功', 'success')
  } catch (err) {
    showToast(err.message || '上传失败', 'error')
  } finally {
    uploading.value = false
  }
}

const removePhoto = (i) => {
  const next = [...photos.value]
  next.splice(i, 1)
  photos.value = next
}

const validate = () => {
  errors.value = { situation: '', result: '' }
  let ok = true
  if (!situation.value.trim()) {
    errors.value.situation = '请填写现场情况描述'
    ok = false
  }
  if (!result.value) {
    errors.value.result = '请选择处理结果'
    ok = false
  }
  return ok
}

const submit = async () => {
  if (!validate()) {
    showToast('请完善必填项', 'error')
    return
  }
  submitting.value = true
  try {
    const parts = [
      `【现场】${situation.value.trim()}`,
      `【结果】${resultText[result.value] || result.value}`
    ]
    if (remark.value.trim()) parts.push(`【备注】${remark.value.trim()}`)

    await issueAPI.create({
      title: `跟进：${task.value?.title || '指派任务'}`,
      reservoirName: task.value?.reservoirName,
      description: parts.join('\n'),
      issueType: 'patrol',
      status: result.value === 'ongoing' ? 'pending' : 'resolved',
      photos: photos.value.length ? JSON.stringify(photos.value) : '',
      patrolTaskId: Number(route.params.id)
    })
    await patrolAPI.completeTask(route.params.id)
    showToast('处理完成，任务已标记完成', 'success')
    setTimeout(() => router.replace('/inspector/tasks?type=assigned'), 800)
  } catch (e) {
    showToast(e.message || '提交失败（请确认已打卡）', 'error')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.loading-tip {
  text-align: center;
  padding: 40px;
  color: var(--color-text-muted);
}
.followup {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 8px;
}
.panel {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 14px 16px;
  box-shadow: var(--shadow-card);
}
.panel__head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.panel__title {
  margin: 0;
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}
.panel__title--solo {
  margin-bottom: 14px;
}
.followup__name {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.35;
}
.info-list {
  display: flex;
  flex-direction: column;
}
.info-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--color-border);
  font-size: 14px;
}
.info-row:last-child {
  border-bottom: none;
}
.info-row__label {
  flex-shrink: 0;
  color: var(--color-text-secondary);
}
.info-row__value {
  text-align: right;
  color: var(--color-text);
  word-break: break-all;
}
.note-box {
  margin-top: 12px;
  padding: 12px;
  border-radius: 8px;
  background: var(--color-primary-light);
  border: 1px solid rgba(37, 99, 235, 0.12);
}
.note-box__label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: 6px;
}
.note-box__text {
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: var(--color-text);
  white-space: pre-wrap;
}
.field {
  margin-bottom: 16px;
}
.field:last-of-type {
  margin-bottom: 0;
}
.field__label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}
.req {
  color: var(--color-danger);
}
.field__error {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--color-danger);
}
.field__hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--color-text-muted);
}
.result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.result-chip {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  text-align: left;
}
.result-chip__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
}
.result-chip__desc {
  font-size: 11px;
  color: var(--color-text-secondary);
  line-height: 1.3;
}
.result-chip--active {
  border-color: var(--color-primary);
  background: var(--color-primary-light);
  box-shadow: inset 0 0 0 1px var(--color-primary);
}
.result-chip--active.result-chip--warn {
  border-color: var(--color-warning);
  background: var(--color-warning-bg);
  box-shadow: inset 0 0 0 1px var(--color-warning);
}
.result-chip--active.result-chip--ok {
  border-color: var(--color-success);
  background: var(--color-success-bg);
  box-shadow: inset 0 0 0 1px var(--color-success);
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
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 14px;
  line-height: 1;
  cursor: pointer;
}
.followup__actions {
  margin-top: 4px;
}
.followup__tip {
  margin: 10px 2px 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--color-text-muted);
}
</style>
