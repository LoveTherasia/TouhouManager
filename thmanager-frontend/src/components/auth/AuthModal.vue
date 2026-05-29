<template>
  <AppModal
    :model-value="modelValue"
    :title="authMode === 'login' ? '登录' : '注册'"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="reset"
  >
    <div v-if="authMode === 'login'" class="auth-form">
      <AppInput v-model="loginForm.usernameOrEmail" label="用户名 / 邮箱" placeholder="请输入用户名或邮箱" />
      <AppInput v-model="loginForm.password" label="密码" type="password" placeholder="请输入密码" />
      <AppButton block :loading="loginLoading" @click="handleLogin">登录</AppButton>
      <p class="auth-switch" @click="authMode = 'register'">还没有账号？立即注册</p>
    </div>

    <div v-else class="auth-form">
      <AppInput v-model="registerForm.username" label="用户名" placeholder="3-20 位字符" />
      <AppInput v-model="registerForm.email" label="邮箱" type="email" placeholder="请输入邮箱" />
      <div class="auth-code-row">
        <AppInput v-model="registerForm.verificationCode" label="验证码" placeholder="请输入验证码" />
        <AppButton
          variant="secondary"
          size="sm"
          :disabled="codeCountdown > 0 || !registerForm.email"
          @click="handleSendCode"
        >
          {{ codeCountdown > 0 ? `${codeCountdown}s` : '发送' }}
        </AppButton>
      </div>
      <AppInput v-model="registerForm.password" label="密码" type="password" placeholder="6-15 位字符" />
      <AppButton block :loading="registerLoading" @click="handleRegister">注册</AppButton>
      <p class="auth-switch" @click="authMode = 'login'">已有账号？立即登录</p>
    </div>
  </AppModal>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { authAPI } from '@/api/auth'
import AppModal from '@/components/ui/AppModal.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppButton from '@/components/ui/AppButton.vue'

defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const userStore = useUserStore()
const authMode = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)
const codeCountdown = ref(0)

const loginForm = ref({ usernameOrEmail: '', password: '' })
const registerForm = ref({ username: '', email: '', verificationCode: '', password: '' })

const reset = () => {
  authMode.value = 'login'
  loginForm.value = { usernameOrEmail: '', password: '' }
  registerForm.value = { username: '', email: '', verificationCode: '', password: '' }
}

const handleLogin = async () => {
  if (!loginForm.value.usernameOrEmail || !loginForm.value.password) {
    alert('请填写完整信息')
    return
  }
  loginLoading.value = true
  try {
    const res = await authAPI.login(loginForm.value)
    if (res.code === 200) {
      userStore.setToken(res.data.accessToken)
      userStore.setUser(res.data.user)
      emit('update:modelValue', false)
      reset()
    } else {
      alert(res.message || '登录失败')
    }
  } catch (error) {
    alert(error.response?.data?.message || '登录失败，请稍后重试')
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  const f = registerForm.value
  if (!f.username || !f.email || !f.password || !f.verificationCode) {
    alert('请填写完整信息')
    return
  }
  registerLoading.value = true
  try {
    const res = await authAPI.register(f)
    if (res.code === 200) {
      userStore.setToken(res.data.accessToken)
      userStore.setUser(res.data.user)
      emit('update:modelValue', false)
      reset()
    } else {
      alert(res.message || '注册失败')
    }
  } catch (error) {
    alert(error.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    registerLoading.value = false
  }
}

const handleSendCode = async () => {
  if (!registerForm.value.email) return
  try {
    const res = await authAPI.sendCode(registerForm.value.email)
    if (res.code === 200) {
      codeCountdown.value = 60
      const timer = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) clearInterval(timer)
      }, 1000)
    } else {
      alert(res.message || '发送失败')
    }
  } catch (error) {
    alert(error.response?.data?.message || '发送失败，请稍后重试')
  }
}
</script>

<style scoped>
.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.auth-code-row {
  display: flex;
  gap: var(--space-3);
  align-items: flex-end;
}
.auth-code-row :deep(.app-input-wrap) { flex: 1; }

.auth-switch {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-accent-gold);
  cursor: pointer;
  margin-top: var(--space-2);
}
.auth-switch:hover { text-decoration: underline; }
</style>
