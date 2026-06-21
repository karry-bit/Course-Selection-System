<template>
  <div>
    <!-- 页面标题 -->
    <div class="mb-6">
      <h2 class="text-2xl font-bold text-slate-800">系统概览</h2>
      <p class="text-sm text-slate-500 mt-1">实时监控选课系统运行状态</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="flex items-center gap-3 text-slate-500">
        <svg class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <span>加载中...</span>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <div v-else class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
      <!-- 学生统计 -->
      <div class="bg-white rounded-2xl border border-slate-200 p-5 hover:shadow-lg hover:shadow-slate-200/50 transition-all duration-300 group">
        <div class="flex items-start justify-between">
          <div class="w-12 h-12 bg-emerald-100 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
            <svg class="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </div>
          <span class="text-xs font-medium text-emerald-600 bg-emerald-50 px-2 py-1 rounded-full">学生</span>
        </div>
        <div class="mt-4">
          <p class="text-3xl font-bold text-slate-800">{{ stats.totalStudents || 0 }}</p>
          <p class="text-sm text-slate-500 mt-1">在校学生</p>
        </div>
      </div>
      
      <!-- 教师统计 -->
      <div class="bg-white rounded-2xl border border-slate-200 p-5 hover:shadow-lg hover:shadow-slate-200/50 transition-all duration-300 group">
        <div class="flex items-start justify-between">
          <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
            <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
            </svg>
          </div>
          <span class="text-xs font-medium text-blue-600 bg-blue-50 px-2 py-1 rounded-full">教师</span>
        </div>
        <div class="mt-4">
          <p class="text-3xl font-bold text-slate-800">{{ stats.totalTeachers || 0 }}</p>
          <p class="text-sm text-slate-500 mt-1">授课教师</p>
        </div>
      </div>
      
      <!-- 课程统计 -->
      <div class="bg-white rounded-2xl border border-slate-200 p-5 hover:shadow-lg hover:shadow-slate-200/50 transition-all duration-300 group">
        <div class="flex items-start justify-between">
          <div class="w-12 h-12 bg-amber-100 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
            <svg class="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
            </svg>
          </div>
          <span class="text-xs font-medium text-amber-600 bg-amber-50 px-2 py-1 rounded-full">课程</span>
        </div>
        <div class="mt-4">
          <p class="text-3xl font-bold text-slate-800">{{ stats.totalCourses || 0 }}</p>
          <p class="text-sm text-slate-500 mt-1">开设课程</p>
        </div>
      </div>
      
      <!-- 选课记录 -->
      <div class="bg-white rounded-2xl border border-slate-200 p-5 hover:shadow-lg hover:shadow-slate-200/50 transition-all duration-300 group">
        <div class="flex items-start justify-between">
          <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
            <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
            </svg>
          </div>
          <span class="text-xs font-medium text-purple-600 bg-purple-50 px-2 py-1 rounded-full">选课</span>
        </div>
        <div class="mt-4">
          <p class="text-3xl font-bold text-slate-800">{{ stats.totalEnrollments || 0 }}</p>
          <p class="text-sm text-slate-500 mt-1">选课记录</p>
        </div>
      </div>
    </div>
    
    <!-- 最近活动 -->
    <div v-if="stats.recentActivities && stats.recentActivities.length" class="bg-white rounded-2xl border border-slate-200 p-6">
      <div class="flex items-center gap-3 mb-4">
        <div class="w-10 h-10 bg-slate-100 rounded-xl flex items-center justify-center">
          <svg class="w-5 h-5 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h3 class="font-semibold text-slate-800">最近活动</h3>
      </div>
      <ul class="space-y-3">
        <li v-for="(a, i) in stats.recentActivities" :key="i" class="flex items-start gap-3 p-3 bg-slate-50 rounded-xl">
          <div class="w-2 h-2 bg-emerald-500 rounded-full mt-2 flex-shrink-0"></div>
          <div class="flex-1 min-w-0">
            <p class="text-sm text-slate-700">{{ a.detail }}</p>
            <p class="text-xs text-slate-400 mt-1">{{ a.time }}</p>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import api from '../../api'

const stats = ref({})
const loading = ref(false)

let pollTimer = null

onMounted(async () => {
  loading.value = true
  const fetchStats = async () => {
    try {
      stats.value = await api.getAdminStats()
    } catch (e) {
      // ignore
    }
  }
  try {
    await fetchStats()
  } finally {
    loading.value = false
  }
  pollTimer = setInterval(fetchStats, 5000)
})

onUnmounted(() => clearInterval(pollTimer))
</script>