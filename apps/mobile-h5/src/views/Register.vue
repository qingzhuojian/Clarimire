<template>
  <div class="login-page">
    <StarTrailBackground />
    <div class="login-box">
      <h2 class="title">群众注册</h2>
      <p class="subtitle">注册后可上报水库周边问题</p>

      <label class="label">用户名 <span class="required">*</span></label>
      <input v-model="form.username" class="input" placeholder="4-20 位字母或数字" autocomplete="username" />

      <label class="label">姓名 <span class="required">*</span></label>
      <input v-model="form.realName" class="input" placeholder="请输入真实姓名" />

      <label class="label">密码 <span class="required">*</span></label>
      <div class="password-wrap">
        <input
          v-model="form.password"
          class="input password-input"
          :type="showPassword ? 'text' : 'password'"
          placeholder="至少 6 位"
          autocomplete="new-password"
        />
        <button type="button" class="pwd-toggle" @click="showPassword = !showPassword">
          {{ showPassword ? '隐藏' : '显示' }}
        </button>
      </div>

      <label class="label">确认密码 <span class="required">*</span></label>
      <div class="password-wrap">
        <input
          v-model="form.confirmPassword"
          class="input password-input"
          :type="showConfirm ? 'text' : 'password'"
          placeholder="再次输入密码"
          autocomplete="new-password"
        />
        <button type="button" class="pwd-toggle" @click="showConfirm = !showConfirm">
          {{ showConfirm ? '隐藏' : '显示' }}
        </button>
      </div>

      <button class="btn btn-primary" :disabled="loading" @click="submit">
        {{ loading ? '注册中...' : '注册' }}
      </button>
      <p class="register-link">
        已有账号？
        <router-link to="/login">返回登录</router-link>
      </p>
      <p v-if="error" class="error">{{ error }}</p>
      <p v-if="success" class="success">{{ success }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '@/api'
import StarTrailBackground from '@/components/StarTrailBackground.vue'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const showConfirm = ref(false)
const error = ref('')
const success = ref('')
const form = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: ''
})

const validate = () => {
  const username = form.username.trim()
  if (!/^[a-zA-Z0-9_]{4,20}$/.test(username)) {
    return '用户名需 4-20 位字母、数字或下划线'
  }
  if (!form.realName.trim()) {
    return '请输入姓名'
  }
  if (form.password.length < 6) {
    return '密码至少 6 位'
  }
  if (form.password !== form.confirmPassword) {
    return '两次密码不一致'
  }
  return ''
}

const submit = async () => {
  error.value = ''
  success.value = ''
  const msg = validate()
  if (msg) {
    error.value = msg
    return
  }

  loading.value = true
  try {
    await authAPI.register({
      username: form.username.trim(),
      password: form.password,
      realName: form.realName.trim()
    })
    success.value = '注册成功，即将跳转登录…'
    setTimeout(() => router.replace('/login'), 1200)
  } catch (e) {
    error.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>
