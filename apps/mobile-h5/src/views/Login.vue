<template>
  <div class="login-page">
    <StarTrailBackground />
    <div class="login-box">
      <h2 class="title">{{ SYSTEM_BRAND }}</h2>
      <p class="system-name">{{ SYSTEM_SUBTITLE }}</p>
      <p class="subtitle">移动端 · 巡查员 / 群众登录</p>
      <label class="label">用户名</label>
      <input v-model="form.username" class="input" placeholder="inspector1 / public1" autocomplete="username" />
      <label class="label">密码</label>
      <div class="password-wrap">
        <input
          v-model="form.password"
          class="input password-input"
          :type="showPassword ? 'text' : 'password'"
          placeholder="请输入密码"
          autocomplete="current-password"
        />
        <button type="button" class="pwd-toggle" @click="showPassword = !showPassword">
          {{ showPassword ? '隐藏' : '显示' }}
        </button>
      </div>
      <button class="btn btn-primary" :disabled="loading" @click="login">{{ loading ? '登录中...' : '登录' }}</button>
      <p class="register-link">
        还没有账号？
        <router-link to="/register">群众注册</router-link>
      </p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '@/api'
import StarTrailBackground from '@/components/StarTrailBackground.vue'
import { SYSTEM_BRAND, SYSTEM_SUBTITLE } from '@/config/brand'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const error = ref('')
const form = reactive({
  username: '',
  password: ''
})

const login = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await authAPI.login({ ...form, clientType: 'mobile' })
    const d = res.data
    localStorage.setItem('token', d.token)
    localStorage.setItem('username', d.username)
    localStorage.setItem('realName', d.realName || '')
    localStorage.setItem('role', d.role)
    localStorage.setItem('userId', String(d.userId))
    localStorage.setItem('allowRemoteCheckin', String(d.allowRemoteCheckin || 0))
    if (d.role === 'inspector') router.replace('/inspector')
    else if (d.role === 'public') router.replace('/public')
    else error.value = '该账号不支持移动端'
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.system-name {
  text-align: center;
  margin: 0 0 8px;
  font-size: 12px;
  line-height: 1.45;
  color: var(--color-primary);
}
</style>
