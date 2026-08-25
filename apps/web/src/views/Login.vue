<template>
  <div class="login-container">
    <StarTrailBackground />
    <div class="login-box">
      <div class="login-header">
        <h1>{{ SYSTEM_BRAND }}</h1>
        <p class="system-name">{{ SYSTEM_SUBTITLE }}</p>
        <p class="quote">人离合，月圆缺，花开又花谢，守护一江清水。</p>
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '@/api'
import StarTrailBackground from '@/components/StarTrailBackground.vue'
import { SYSTEM_BRAND, SYSTEM_SUBTITLE } from '@/config/brand'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await authAPI.login({ ...loginForm, clientType: 'web' })
        if (res.code === 200) {
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('username', res.data.username)
          localStorage.setItem('realName', res.data.realName)
          localStorage.setItem('role', res.data.role)
          ElMessage.success('登录成功')
          router.push('/home')
        } else {
          ElMessage.error(res.message || '登录失败')
        }
      } catch (error) {
        console.error('Login error:', error)
        ElMessage.error(error.message || '登录失败，请确认后端已启动')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.login-box {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: 36px 40px 32px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(8px);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-header h1 {
  font-size: 30px;
  color: #1a1a1a;
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.system-name {
  margin: 0 0 14px;
  font-size: 13px;
  color: #4a6fa5;
  line-height: 1.45;
  letter-spacing: 0.5px;
}

.quote {
  font-size: 13px;
  color: #888;
  line-height: 1.6;
  padding: 8px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.login-btn {
  width: 100%;
  border-radius: 24px;
  height: 42px;
}
</style>
