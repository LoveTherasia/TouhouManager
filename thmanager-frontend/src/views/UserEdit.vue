<template>
  <div class="user-edit-container">
    <h2>编辑用户信息</h2>
    <div class="user-edit-form">
      <div class="form-group">
        <label>用户名</label>
        <input v-model="userForm.username" type="text" placeholder="请输入用户名" />
      </div>
      <div class="form-group">
        <label>邮箱</label>
        <input v-model="userForm.email" type="email" placeholder="请输入邮箱" />
      </div>
      <div class="form-group">
        <label>头像</label>
        <div class="avatar-upload">
          <img v-if="userForm.avatarUrl" :src="userForm.avatarUrl" alt="头像" class="current-avatar" />
          <span v-else class="avatar-placeholder">上传头像</span>
          <input type="file" ref="fileInput" @change="handleAvatarUpload" style="display: none" />
          <button type="button" class="upload-button" @click="$refs.fileInput.click()">选择头像</button>
        </div>
      </div>
      <div class="form-actions">
        <button class="save-button" @click="updateUserProfile" :disabled="loading">
          {{ loading ? '保存中...' : '保存' }}
        </button>
        <button class="cancel-button" @click="goBack">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { authAPI } from '../api/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const userForm = ref({
  username: '',
  email: '',
  avatarUrl: ''
})

// 获取用户信息
const fetchUserProfile = async () => {
  try {
    const res = await authAPI.getUserProfile()
    if (res.code === 200) {
      userForm.value = res.data
    } else {
      alert(res.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
    alert(error.response?.data?.message || '获取用户信息失败，请稍后重试')
  }
}

// 更新用户信息
const updateUserProfile = async () => {
  if (!userForm.value.username || !userForm.value.email) {
    alert('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const res = await authAPI.updateUserProfile(userForm.value)
    if (res.code === 200) {
      userStore.setUser(res.data)
      alert('更新成功')
      router.push('/')
    } else {
      alert(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新用户信息错误:', error)
    alert(error.response?.data?.message || '更新失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理头像上传
const handleAvatarUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    // 这里可以实现头像上传逻辑，暂时使用本地预览
    const reader = new FileReader()
    reader.onload = (e) => {
      userForm.value.avatarUrl = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

// 返回首页
const goBack = () => {
  router.push('/')
}

// 页面加载时获取用户信息
onMounted(async () => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn()) {
    alert('请先登录')
    router.push('/')
    return
  }
  await fetchUserProfile()
})
</script>

<style scoped>
.user-edit-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.user-edit-container h2 {
  color: #fff;
  margin-bottom: 30px;
  font-size: 24px;
}

.user-edit-form {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 40px;
  min-width: 400px;
  max-width: 90vw;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.form-group input:focus {
  border-color: rgba(102, 126, 234, 0.6);
  background: rgba(255, 255, 255, 0.1);
}

.form-group input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.current-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  text-align: center;
}

.upload-button {
  padding: 8px 16px;
  border: 1px solid rgba(102, 126, 234, 0.5);
  border-radius: 8px;
  background: rgba(102, 126, 234, 0.2);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-button:hover {
  background: rgba(102, 126, 234, 0.4);
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 30px;
}

.save-button {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.save-button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.save-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.cancel-button {
  padding: 12px 24px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
}

@media (max-width: 768px) {
  .user-edit-form {
    min-width: 100%;
    padding: 20px;
  }
}
</style>