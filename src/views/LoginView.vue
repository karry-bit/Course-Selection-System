<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-50 px-4">
    <!-- 背景装饰 -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-emerald-100 rounded-full opacity-50 blur-3xl"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-slate-200 rounded-full opacity-50 blur-3xl"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="relative w-full max-w-[400px]">
      <!-- Logo 区域 -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-emerald-600 rounded-2xl shadow-lg shadow-emerald-200 mb-4">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-slate-800">高校选课管理系统</h1>
        <p class="text-sm text-slate-500 mt-1">Course Selection System</p>
      </div>
      
      <!-- 登录表单 -->
      <div class="bg-white rounded-2xl shadow-xl shadow-slate-200/50 p-8 border border-slate-100">
        <!-- 角色切换 -->
        <div class="flex bg-slate-100 rounded-xl p-1 mb-6">
          <button
            v-for="r in roles"
            :key="r.value"
            @click="form.role = r.value"
            class="flex-1 py-2 text-sm font-medium rounded-lg transition-all duration-200"
            :class="[
              form.role === r.value
                ? 'bg-white text-emerald-700 shadow-sm'
                : 'text-slate-500 hover:text-slate-700'
            ]"
          >
            {{ r.label }}
          </button>
        </div>
        
        <!-- 表单 -->
        <div class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-2">
              {{ form.role === 'ADMIN' ? '用户名' : form.role === 'STUDENT' ? '学号' : '工号' }}
            </label>
            <input
              v-model="form.username"
              type="text"
              class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 outline-none transition-all duration-200"
              :placeholder="form.role === 'ADMIN' ? 'admin' : form.role === 'STUDENT' ? '请输入学号' : '请输入工号'"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-2">密码</label>
            <input
              v-model="form.password"
              type="password"
              class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 outline-none transition-all duration-200"
              placeholder="请输入密码"
              @keyup.enter="login"
            />
          </div>
          
          <!-- 错误提示 -->
          <p v-if="error" class="text-red-500 text-sm bg-red-50 px-4 py-2 rounded-lg">
            {{ error }}
          </p>
          
          <!-- 登录按钮 -->
          <button
            @click="login"
            :disabled="loading"
            class="w-full py-3 bg-emerald-600 text-white rounded-xl font-medium hover:bg-emerald-700 active:scale-[0.98] transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-emerald-200"
          >
            <span v-if="loading" class="flex items-center justify-center gap-2">
              <svg class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              登录中...
            </span>
            <span v-else>登 录</span>
          </button>
        </div>
        
        <!-- 测试账号提示 -->
        <div class="mt-6 pt-6 border-t border-slate-100">
          <p class="text-xs text-slate-400 text-center">
            测试账号：admin / admin123
          </p>
          <p class="text-xs text-slate-400 text-center mt-1">
            学生/教师：工号学号 / 123456
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()

const roles = [
  { label: '学生', value: 'STUDENT' },
  { label: '教师', value: 'TEACHER' },
  { label: '管理员', value: 'ADMIN' }
]

const form = reactive({ username: '', password: '', role: 'STUDENT' })
const loading = ref(false)
const error = ref('')

async function login() {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await api.login(form.username, form.password)
    localStorage.setItem('token', res.token)
    localStorage.setItem('role', res.role)
    localStorage.setItem('name', res.name || '')
    localStorage.setItem('userId', res.userId || '')
    router.push(`/${res.role.toLowerCase()}`)
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>