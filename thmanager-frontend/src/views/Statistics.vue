<template>
  <div class="statistics-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="统计分析" name="statistics">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>游戏时长统计</template>
              <div ref="playTimeChart" style="height: 300px;"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>难度分布</template>
              <div ref="difficultyChart" style="height: 300px;"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="mt-20">
          <el-col :span="24">
            <el-card>
              <template #header>分数趋势</template>
              <div ref="scoreChart" style="height: 350px;"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane label="Replays" name="replays">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Replay 列表</span>
              <el-input 
                v-model="replayFilter" 
                placeholder="搜索 Replay" 
                style="width: 200px;"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </template>
          <el-table :data="filteredReplays" v-loading="replaysLoading" stripe>
            <el-table-column type="index" width="50" />
            <el-table-column prop="gameTitle" label="游戏" min-width="150" />
            <el-table-column prop="playerName" label="玩家" min-width="100" />
            <el-table-column prop="difficultyDisplay" label="难度" width="100" />
            <el-table-column prop="totalScore" label="分数" width="120" formatter="formatScore" />
            <el-table-column prop="stage" label="关卡" width="80" />
            <el-table-column prop="date" label="日期" width="150" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="viewReplay(row)">
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination" v-if="replaysStore.replays.length > 0">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="replaysStore.replays.length"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  DatasetComponent
} from 'echarts/components'
import * as echarts from 'echarts/core'
import { useStatisticsStore } from '../stores/statistics'
import { useGamesStore } from '../stores/games'
import { useReplaysStore } from '../stores/replays'
import { Search, View } from '@element-plus/icons-vue'

use([
  CanvasRenderer,
  BarChart,
  PieChart,
  LineChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
  DatasetComponent
])

const statsStore = useStatisticsStore()
const gamesStore = useGamesStore()
const replaysStore = useReplaysStore()

// 标签页
const activeTab = ref('statistics')

// Replays 相关
const replayFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const replaysLoading = ref(false)

// 图表相关
const playTimeChart = ref(null)
const difficultyChart = ref(null)
const scoreChart = ref(null)

let playTimeChartInstance = null
let difficultyChartInstance = null
let scoreChartInstance = null

// 过滤后的 Replays
const filteredReplays = computed(() => {
  let filtered = replaysStore.replays
  if (replayFilter.value) {
    const filter = replayFilter.value.toLowerCase()
    filtered = filtered.filter(replay => 
      (replay.gameTitle || '').toLowerCase().includes(filter) ||
      (replay.playerName || '').toLowerCase().includes(filter) ||
      (replay.difficultyDisplay || '').toLowerCase().includes(filter)
    )
  }
  // 分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filtered.slice(start, end)
})

// 格式化分数
const formatScore = (row, column, cellValue) => {
  if (!cellValue) return '0'
  return (cellValue / 100000000).toFixed(1) + '亿'
}

// 查看 Replay 详情
const viewReplay = (row) => {
  console.log('查看 Replay:', row)
  // 这里可以添加查看 Replay 详情的逻辑
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (current) => {
  currentPage.value = current
}

const initPlayTimeChart = () => {
  if (!playTimeChart.value) return
  
  playTimeChartInstance = echarts.init(playTimeChart.value)
  const games = gamesStore.installedGames
  
  playTimeChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: games.map(g => g.displayName?.split(' - ')[1] || g.displayName),
      axisLabel: { rotate: 45 }
    },
    yAxis: { type: 'value', name: '小时' },
    series: [{
      data: games.map(g => {
        const hours = parseInt(g.totalPlayTime?.split('小时')[0] || 0)
        return hours
      }),
      type: 'bar',
      itemStyle: { color: '#409EFF' }
    }]
  })
}

const initDifficultyChart = () => {
  if (!difficultyChart.value) return
  
  difficultyChartInstance = echarts.init(difficultyChart.value)
  const replays = replaysStore.replays
  
  const difficultyCount = {}
  replays.forEach(r => {
    const diff = r.difficultyDisplay || 'Unknown'
    difficultyCount[diff] = (difficultyCount[diff] || 0) + 1
  })
  
  const data = Object.entries(difficultyCount).map(([name, value]) => ({ name, value }))
  
  difficultyChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '60%',
      data,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  })
}

const initScoreChart = () => {
  if (!scoreChart.value) return
  
  scoreChartInstance = echarts.init(scoreChart.value)
  const replays = replaysStore.replays.slice(0, 20)
  
  scoreChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: replays.map((r, i) => `#${i + 1}`)
    },
    yAxis: { 
      type: 'value',
      axisLabel: {
        formatter: (value) => (value / 100000000).toFixed(1) + '亿'
      }
    },
    series: [{
      data: replays.map(r => r.totalScore || 0),
      type: 'line',
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ]
        }
      },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

const handleResize = () => {
  playTimeChartInstance?.resize()
  difficultyChartInstance?.resize()
  scoreChartInstance?.resize()
}

onMounted(async () => {
  replaysLoading.value = true
  try {
    await Promise.all([
      statsStore.fetchStatistics(),
      gamesStore.fetchGames(),
      replaysStore.fetchReplays()
    ])
    
    initPlayTimeChart()
    initDifficultyChart()
    initScoreChart()
  } finally {
    replaysLoading.value = false
  }
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  playTimeChartInstance?.dispose()
  difficultyChartInstance?.dispose()
  scoreChartInstance?.dispose()
})
</script>

<style scoped>
.statistics-page {
  padding: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
