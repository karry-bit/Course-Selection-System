<template>
  <div class="min-h-screen bg-slate-50">
    <!-- 顶部导航栏 -->
    <header class="bg-white border-b border-slate-200 sticky top-0 z-40">
      <div class="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
        <!-- Logo -->
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 bg-emerald-600 rounded-xl flex items-center justify-center shadow-lg shadow-emerald-100">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
            </svg>
          </div>
          <span class="text-lg font-bold text-slate-800">教师工作台</span>
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
            @click="showChangePwd = true" 
            class="text-sm text-slate-500 hover:text-emerald-600 transition-colors"
          >
            修改密码
          </button>
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

    <!-- 修改密码弹窗 -->
    <div v-if="showChangePwd" class="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50" @click.self="showChangePwd = false">
      <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-md mx-4 animate-in fade-in zoom-in-95 duration-200">
        <div class="flex items-center gap-3 mb-6">
          <div class="w-10 h-10 bg-emerald-100 rounded-xl flex items-center justify-center">
            <svg class="w-5 h-5 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
            </svg>
          </div>
          <h3 class="text-lg font-bold text-slate-800">修改密码</h3>
        </div>
        <div class="space-y-4">
          <input 
            v-model="pwdForm.oldPassword" 
            type="password" 
            placeholder="原密码" 
            class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 outline-none transition-all" 
          />
          <input 
            v-model="pwdForm.newPassword" 
            type="password" 
            placeholder="新密码" 
            class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 outline-none transition-all" 
          />
          <input 
            v-model="pwdForm.confirmPassword" 
            type="password" 
            placeholder="确认新密码" 
            class="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:bg-white focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 outline-none transition-all" 
          />
        </div>
        <p v-if="pwdMsg" :class="pwdMsgType === 'error' ? 'text-red-500 bg-red-50' : 'text-green-600 bg-green-50'" class="text-sm px-4 py-2 rounded-xl mt-3">
          {{ pwdMsg }}
        </p>
        <div class="flex justify-end gap-3 mt-6">
          <button 
            @click="showChangePwd = false" 
            class="px-5 py-2.5 bg-slate-100 text-slate-700 text-sm font-medium rounded-xl hover:bg-slate-200 transition-all"
          >
            取消
          </button>
          <button 
            @click="changePassword" 
            :disabled="pwdSaving" 
            class="px-5 py-2.5 bg-emerald-600 text-white text-sm font-medium rounded-xl hover:bg-emerald-700 disabled:opacity-50 transition-all"
          >
            {{ pwdSaving ? '修改中...' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../api'

const router = useRouter()
const name = ref(localStorage.getItem('name') || '老师')

const nav = [
  { label: '我的课程', to: '/teacher/courses' },
  { label: '成绩录入', to: '/teacher/scores' }
]

const showChangePwd = ref(false)
const pwdSaving = ref(false)
const pwdMsg = ref('')
const pwdMsgType = ref('success')
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

function logout() {
  localStorage.clear()
  router.push('/login')
}

async function changePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    pwdMsg.value = '请填写所有字段'
    pwdMsgType.value = 'error'
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    pwdMsg.value = '两次输入的密码不一致'
    pwdMsgType.value = 'error'
    return
  }
  
  pwdSaving.value = true
  try {
    await api.teacherChangePassword(pwdForm.oldPassword, pwdForm.newPassword)
    pwdMsg.value = '密码修改成功'
    pwdMsgType.value = 'success'
    setTimeout(() => {
      showChangePwd.value = false
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
      pwdMsg.value = ''
    }, 1500)
  } catch (e) {
    pwdMsg.value = e.message || '修改失败'
    pwdMsgType.value = 'error'
  } finally {
    pwdSaving.value = false
  }
}
</script>