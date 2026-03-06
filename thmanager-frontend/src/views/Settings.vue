<template>
  <div class="settings-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>游戏路径设置</span>
          <el-button type="primary" @click="saveSettings">
            <el-icon><Check /></el-icon>
            保存设置
          </el-button>
        </div>
      </template>

      <el-alert
        title="提示"
        description="请设置各游戏的安装路径，设置后系统会自动检测游戏是否已安装并扫描 Replay 文件。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-table :data="gameSettings" v-loading="loading" stripe>
        <el-table-column type="index" width="50" />
        <el-table-column prop="displayName" label="游戏" min-width="200" />
        <el-table-column label="安装路径" min-width="300">
          <template #default="{ row }">
            <el-input
              v-model="row.installPath"
              placeholder="请选择游戏安装目录"
              readonly
            >
              <template #append>
                <el-button @click="selectPath(row)">
                  <el-icon><Folder /></el-icon>
                </el-button>
              </template>
            </el-input>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.installPath ? 'success' : 'info'">
              {{ row.installPath ? '已设置' : '未设置' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 批量设置对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量设置路径" width="500px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="根目录">
          <el-input v-model="batchForm.rootPath" placeholder="例如: D:\\Touhou">
            <template #append>
              <el-button @click="selectRootPath">
                <el-icon><Folder /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="命名规则">
          <el-select v-model="batchForm.pattern" style="width: 100%;">
            <el-option label="THxx 格式 (如: TH06, TH07)" value="thxx" />
            <el-option label="中文名格式" value="chinese" />
            <el-option label="日文名格式" value="japanese" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="applyBatch">应用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElTable } from 'element-plus'
import * as settingsApi from '../api/settings'

const route = useRoute()
const loading = ref(false)
const gameSettings = ref([])
const batchDialogVisible = ref(false)
const batchForm = ref({
  rootPath: '',
  pattern: 'thxx'
})

const fetchSettings = async () => {
  loading.value = true
  try {
    const data = await settingsApi.getAllGames()
    gameSettings.value = data.map(game => ({
      ...game,
      installPath: game.installPath || ''
    }))
    
    // 如果有 gameId 参数，滚动到对应游戏行
    const gameId = route.query.gameId
    if (gameId) {
      setTimeout(() => {
        const table = document.querySelector('.el-table')
        if (table) {
          const rows = table.querySelectorAll('.el-table__row')
          rows.forEach((row, index) => {
            if (gameSettings.value[index] && gameSettings.value[index].id == gameId) {
              row.scrollIntoView({ behavior: 'smooth', block: 'center' })
              // 高亮显示该行
              row.classList.add('el-table__row--highlight')
            }
          })
        }
      }, 500)
    }
  } catch (error) {
    console.error('Failed to fetch settings:', error)
    ElMessage.error('加载设置失败')
  } finally {
    loading.value = false
  }
}

const selectPath = async (game) => {
  // 在 Web 环境中使用 input 模拟文件夹选择
  const input = document.createElement('input')
  input.type = 'file'
  input.webkitdirectory = true
  input.onchange = (e) => {
    if (e.target.files.length > 0) {
      const path = e.target.files[0].path || e.target.files[0].webkitRelativePath.split('/')[0]
      game.installPath = path
    }
  }
  input.click()
}

const selectRootPath = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.webkitdirectory = true
  input.onchange = (e) => {
    if (e.target.files.length > 0) {
      batchForm.value.rootPath = e.target.files[0].path
    }
  }
  input.click()
}

const saveSettings = async () => {
  try {
    for (const game of gameSettings.value) {
      if (game.installPath) {
        await settingsApi.updateGameSettings(game.id, {
          installPath: game.installPath
        })
      }
    }
    ElMessage.success('设置已保存')
  } catch (error) {
    console.error('Failed to save settings:', error)
    ElMessage.error('保存失败')
  }
}

const applyBatch = () => {
  const { rootPath, pattern } = batchForm.value
  if (!rootPath) {
    ElMessage.warning('请先选择根目录')
    return
  }

  gameSettings.value.forEach(game => {
    let folderName = ''
    if (pattern === 'thxx') {
      folderName = `TH${String(game.gameNumber).padStart(2, '0')}`
    } else if (pattern === 'chinese') {
      folderName = game.titleCn || game.titleJa
    } else {
      folderName = game.titleJa
    }
    game.installPath = `${rootPath}\${folderName}`
  })

  batchDialogVisible.value = false
  ElMessage.success('批量设置完成，请检查路径后保存')
}

onMounted(() => {
  fetchSettings()
})
</script>

<style scoped>
.settings-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
