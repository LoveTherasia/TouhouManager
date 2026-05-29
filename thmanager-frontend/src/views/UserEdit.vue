<template>
  <AppShell title="编辑资料" subtitle="管理你的账号信息">
    <AppCard class="profile-card">
      <AppLoading v-if="loading" />
      <div v-else class="profile-form">
        <div class="avatar-section">
          <div class="avatar-preview">
            <img v-if="userForm.avatarUrl" :src="userForm.avatarUrl" alt="头像" />
            <span v-else class="avatar-placeholder">头像</span>
          </div>
          <input ref="fileInput" type="file" accept="image/jpeg,image/png" hidden @change="handleAvatarUpload" />
          <AppButton variant="secondary" size="sm" @click="$refs.fileInput.click()">更换头像</AppButton>
        </div>

        <AppInput v-model="userForm.username" label="用户名" placeholder="请输入用户名" />
        <AppInput v-model="userForm.email" label="邮箱" type="email" placeholder="请输入邮箱" />

        <div class="profile-actions">
          <AppButton variant="ghost" @click="router.push('/')">取消</AppButton>
          <AppButton :loading="saving" @click="updateUserProfile">保存</AppButton>
        </div>
      </div>
    </AppCard>
  </AppShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authAPI } from '@/api/auth'
import AppShell from '@/components/layout/AppShell.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppLoading from '@/components/ui/AppLoading.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const userForm = ref({ username: '', email: '', avatarUrl: '' })

const fetchUserProfile = async () => {
  try {
    const res = await authAPI.getUserProfile()
    if (res.code === 200) userForm.value = res.data
  } catch {
    alert('获取用户信息失败')
  }
}

const updateUserProfile = async () => {
  if (!userForm.value.username || !userForm.value.email) {
    alert('请填写完整信息')
    return
  }
  saving.value = true
  try {
    const res = await authAPI.updateUserProfile(userForm.value)
    if (res.code === 200) {
      userStore.setUser(res.data)
      router.push('/')
    } else {
      alert(res.message || '更新失败')
    }
  } catch {
    alert('更新失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const handleAvatarUpload = (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    alert('请上传 JPG 或 PNG 格式图片')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    alert('图片大小不能超过 2MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (ev) => { userForm.value.avatarUrl = ev.target.result }
  reader.readAsDataURL(file)
}

onMounted(async () => {
  if (!userStore.isLoggedIn()) {
    router.push('/')
    return
  }
  loading.value = true
  await fetchUserProfile()
  loading.value = false
})
</script>

<style scoped>
.profile-card { max-width: 520px; }

.profile-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.avatar-preview {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid var(--color-border-active);
}
.avatar-preview img { width: 100%; height: 100%; object-fit: cover; }

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: var(--color-bg-elevated);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.profile-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-2);
}
</style>
