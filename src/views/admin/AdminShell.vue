<template>
  <div class="min-h-screen bg-slate-50">
    <!-- 顶部导航栏 -->
    <header class="bg-white border-b border-slate-200 sticky top-0 z-40">
      <div class="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        <!-- Logo -->
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 bg-emerald-600 rounded-xl flex items-center justify-center shadow-lg shadow-emerald-100">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </div>
          <span class="text-lg font-bold text-slate-800">管理控制台</span>
        </div>
        
        <!-- 导航 -->
        <nav class="flex items-center gap-1">
          <router-link
            v-for="item in nav"
            :key="item.to"
            :to="item.to"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200"
            :class="$route.path === item.to ? 'bg-emerald-50 text-emerald-700' : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'"
          >
            {{ item.label }}
          </router-link>
        </nav>
        
        <!-- 用户信息 -->
        <div class="flex items-center gap-4">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 bg-emerald-100 rounded-full flex items-center justify-center">
              <svg class="w-4 h-4 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <span class="text-sm font-medium text-slate-700">{{ name }}</span>
          </div>
          <button 
            @click="logout" 
            class="text-sm text-red-500 hover:text-red-600 transition-colors"
          >
            退出
          </button>
        </div>
      </div>
    </header>
    
    <!-- 主内容 -->
    <main class="p-6 max-w-7xl mx-auto">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const name = ref(localStorage.getItem('name') || '管理员')

const nav = [
  { label: '仪表盘', to: '/admin/dashboard' },
  { label: '学生', to: '/admin/students' },
  { label: '教师', to: '/admin/teachers' },
  { label: '课程', to: '/admin/courses' },
  { label: '班级', to: '/admin/classes' },
  { label: '选课时段', to: '/admin/term-sessions' }
]

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>