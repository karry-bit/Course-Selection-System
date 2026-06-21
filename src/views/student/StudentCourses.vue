<template>
  <div class="flex gap-6">
    <!-- 左侧课程列表区域 -->
    <div class="flex-1">
    <!-- 搜索和筛选栏 -->
    <div class="flex items-center gap-4 mb-6 flex-wrap">
      <div class="relative flex-1 max-w-md">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <input
          v-model="search"
          @input="debouncedSearch"
          placeholder="搜索课程名称..."
          class="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 transition-all"
        />
      </div>
      <select v-model="semester" @change="loadCourses" class="px-4 py-2.5 bg-white border border-slate-200 rounded-xl outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 transition-all cursor-pointer">
        <option value="">全部学期</option>
        <option v-for="s in semesters" :key="s" :value="s">{{ s }}</option>
      </select>
      <div v-if="termInfo.isOpen" class="flex items-center gap-2 px-4 py-2 bg-emerald-50 text-emerald-700 rounded-xl text-sm font-medium">
        <span class="w-2 h-2 bg-emerald-500 rounded-full animate-pulse"></span>
        选课开放中 · {{ termInfo.name }}
      </div>
      <div v-else class="flex items-center gap-2 px-4 py-2 bg-red-50 text-red-600 rounded-xl text-sm font-medium">
        <span class="w-2 h-2 bg-red-500 rounded-full"></span>
        选课未开放
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="flex items-center gap-3 text-slate-500">
        <svg class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <span>加载课程列表...</span>
      </div>
    </div>
    
    <!-- 课程网格 -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="c in courses"
        :key="c.id"
        class="bg-white rounded-2xl border border-slate-200 p-5 hover:shadow-lg hover:shadow-slate-200/50 transition-all duration-300 group"
        :class="{ 'border-red-200 bg-red-50/30': hasTimeConflict(c) }"
      >
        <!-- 课程头部 -->
        <div class="flex items-start justify-between mb-3">
          <div class="flex-1 min-w-0">
            <h3 class="font-bold text-lg text-slate-800 group-hover:text-emerald-600 transition-colors">{{ c.name }}</h3>
            <p class="text-xs text-slate-400 mt-0.5">{{ c.code }}</p>
          </div>
          <span v-if="hasTimeConflict(c)" class="flex-shrink-0 text-xs bg-red-100 text-red-600 px-2.5 py-1 rounded-full font-medium">
            时间冲突
          </span>
        </div>
        
        <!-- 课程信息 -->
        <div class="space-y-2">
          <div class="flex items-center gap-2 text-sm text-slate-600">
            <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            <span>{{ c.teacher?.name || '未指定' }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-slate-600">
            <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{{ c.timeSlot || '待定' }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-slate-600">
            <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            <span>{{ c.place || '待定' }}</span>
          </div>
          <div class="flex items-center gap-4 text-sm text-slate-500">
            <span class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              {{ c.credit }} 学分
            </span>
            <span class="flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              {{ c.capacity }} 人
            </span>
          </div>
        </div>
        
        <!-- 课程描述 -->
        <p v-if="c.description" class="text-sm text-slate-500 mt-3 line-clamp-2">{{ c.description }}</p>
        
        <!-- 操作按钮 -->
        <button
          v-if="!enrolledIds.includes(c.id)"
          @click="doEnroll(c)"
          class="mt-4 w-full py-2.5 text-sm font-medium rounded-xl transition-all duration-200 disabled:opacity-50"
          :class="hasTimeConflict(c) 
            ? 'bg-amber-500 text-white hover:bg-amber-600 active:scale-[0.98]' 
            : 'bg-emerald-600 text-white hover:bg-emerald-700 active:scale-[0.98]'"
          :disabled="!termInfo.isOpen"
        >
          {{ hasTimeConflict(c) ? '选课（有冲突）' : '立即选课' }}
        </button>
        <button
          v-else
          @click="doDrop(c)"
          class="mt-4 w-full py-2.5 bg-emerald-100 text-emerald-700 text-sm font-medium rounded-xl hover:bg-emerald-200 transition-all disabled:opacity-50 disabled:cursor-not-allowed active:scale-[0.98]"
          :disabled="!termInfo.isOpen"
        >
          {{ termInfo.isOpen ? '退课' : '已选课程' }}
        </button>
      </div>
    </div>
    
    <!-- 消息提示 -->
    <p v-if="msg" :class="msgType === 'error' ? 'text-red-500 bg-red-50' : 'text-emerald-600 bg-emerald-50'" class="mt-4 text-sm px-4 py-3 rounded-xl">
      {{ msg }}
    </p>

    <!-- 冲突提示对话框 -->
    <UiDialog v-model="conflictDialog.show" title="时间冲突提示">
      <div v-if="conflictDialog.newCourse" class="space-y-4">
        <div class="bg-red-50 border border-red-200 rounded-xl p-4">
          <p class="text-red-700 font-bold text-lg mb-2">所选课程与已选课程时间冲突！</p>
          <div class="bg-white rounded-lg p-3 mt-2">
            <p class="font-medium text-slate-700">您正在选择：</p>
            <p class="text-emerald-600 font-bold">{{ conflictDialog.newCourse.name }}</p>
            <p class="text-sm text-slate-400">{{ conflictDialog.newCourse.code }}</p>
            <p class="text-sm text-slate-600 mt-1">时间：{{ conflictDialog.newCourse.timeSlot }}</p>
          </div>
        </div>
        
        <div v-if="conflictDialog.conflicts && conflictDialog.conflicts.length" class="bg-amber-50 border border-amber-200 rounded-xl p-4">
          <p class="text-amber-800 font-bold mb-3">与以下已选课程冲突：</p>
          <div v-for="cc in conflictDialog.conflicts" :key="cc.courseId" class="bg-white rounded-lg p-3 mb-2 last:mb-0">
            <p class="font-medium text-slate-700">{{ cc.courseName }}</p>
            <p class="text-sm text-slate-400">{{ cc.courseCode }}</p>
            <p class="text-sm text-slate-600">时间：{{ cc.timeSlot }}</p>
            <div v-if="cc.conflictDetails && cc.conflictDetails.length" class="mt-2 text-sm">
              <p class="text-red-600 font-medium">冲突详情：</p>
              <ul class="list-disc list-inside text-red-500">
                <li v-for="(detail, idx) in cc.conflictDetails" :key="idx">{{ detail }}</li>
              </ul>
            </div>
          </div>
        </div>
        
        <div class="bg-blue-50 border border-blue-200 rounded-xl p-4">
          <p class="text-blue-700 font-medium">请确认是否继续选课？</p>
          <p class="text-sm text-slate-600 mt-1">如果继续选课，请合理安排时间，避免实际冲突。</p>
        </div>
      </div>
      <template #actions>
        <button
          @click="conflictDialog.show = false"
          class="px-4 py-2.5 bg-slate-100 text-slate-700 font-medium rounded-xl hover:bg-slate-200 transition-all"
        >
          取消
        </button>
        <button
          @click="forceEnroll"
          class="px-4 py-2.5 bg-red-600 text-white font-medium rounded-xl hover:bg-red-700 transition-all"
        >
          仍然选课
        </button>
      </template>
    </UiDialog>
    </div>

    <!-- 右侧选课列表导航栏 -->
    <div v-if="termInfo.isOpen" class="w-80 flex-shrink-0">
      <div class="sticky top-6 bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <!-- 头部 -->
        <div class="bg-gradient-to-r from-emerald-500 to-teal-500 px-5 py-4">
          <h3 class="text-white font-bold text-lg">我的选课列表</h3>
          <div class="flex items-center justify-between mt-2">
            <span class="text-emerald-100 text-sm">已选 {{ selectedCourses.length }} 门课程</span>
            <span class="bg-white/20 text-white text-sm px-3 py-1 rounded-full">{{ totalCredits }} 学分</span>
          </div>
        </div>

        <!-- 选课列表 -->
        <div class="p-4 max-h-[600px] overflow-y-auto custom-scrollbar">
          <div v-if="selectedCourses.length === 0" class="text-center py-8">
            <svg class="w-12 h-12 mx-auto text-slate-300 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            <p class="text-slate-400 text-sm">还没有选任何课程</p>
          </div>

          <div v-else class="space-y-3">
            <div
              v-for="course in selectedCourses"
              :key="course.id"
              class="bg-slate-50 rounded-xl p-3 border border-slate-100 hover:border-emerald-200 transition-all group"
            >
              <div class="flex items-start justify-between mb-2">
                <div class="flex-1 min-w-0">
                  <h4 class="font-semibold text-slate-700 text-sm group-hover:text-emerald-600 transition-colors truncate">{{ course.name }}</h4>
                  <p class="text-xs text-slate-400">{{ course.code }}</p>
                </div>
                <button
                  @click="quickDrop(course)"
                  class="flex-shrink-0 text-slate-400 hover:text-red-500 transition-colors p-1"
                  title="退课"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              <div class="space-y-1 text-xs text-slate-500">
                <div class="flex items-center gap-1">
                  <svg class="w-3 h-3 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span class="truncate">{{ course.teacher?.name || '未指定' }}</span>
                </div>
                <div class="flex items-center gap-1">
                  <svg class="w-3 h-3 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span class="truncate">{{ course.timeSlot }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span>{{ course.credit }} 学分</span>
                  <span v-if="course.place" class="truncate ml-2">{{ course.place }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部统计 -->
        <div v-if="selectedCourses.length > 0" class="border-t border-slate-100 px-5 py-3 bg-slate-50">
          <div class="flex items-center justify-between text-sm">
            <span class="text-slate-500">课程总数</span>
            <span class="font-semibold text-slate-700">{{ selectedCourses.length }} 门</span>
          </div>
          <div class="flex items-center justify-between text-sm mt-1">
            <span class="text-slate-500">总学分</span>
            <span class="font-semibold text-emerald-600">{{ totalCredits }} 学分</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import api from '../../api'
import UiDialog from '../../components/ui/Dialog.vue'

const courses = ref([])
const enrolledIds = ref([])
const myEnrollments = ref([])
const loading = ref(false)
const search = ref('')
const semester = ref('')
const semesters = ref([])
const termInfo = ref({ isOpen: false })
const msg = ref('')
const msgType = ref('success')

// 计算已选课程列表
const selectedCourses = computed(() => {
  return myEnrollments.value
    .filter(e => e.status === 'ENROLLED' || !e.status)
    .map(e => e.course)
    .filter(Boolean)
})

// 计算总学分
const totalCredits = computed(() => {
  return selectedCourses.value.reduce((sum, course) => sum + (course.credit || 0), 0)
})

const conflictDialog = ref({
  show: false,
  newCourse: null,
  conflicts: []
})

let timer = null
function debouncedSearch() {
  clearTimeout(timer)
  timer = setTimeout(loadCourses, 400)
}

function parseTimeSlot(timeSlot) {
  if (!timeSlot) return []
  
  const result = []
  const parts = timeSlot.split(/[;；]/)
  const pattern = /(周[一二三四五六日])(\d+)(?:-(\d+))?节(?:\(([\\d,-]+)周\))?/
  
  for (const part of parts) {
    const match = part.trim().match(pattern)
    if (match) {
      result.push({
        day: match[1],
        sections: match[3] ? `${match[2]}-${match[3]}` : match[2],
        weeks: match[4] || ''
      })
    }
  }
  
  return result
}

function hasTimeConflict(course) {
  if (!course.timeSlot || myEnrollments.value.length === 0) return false
  
  const newSlots = parseTimeSlot(course.timeSlot)
  if (newSlots.length === 0) return false
  
  for (const enrollment of myEnrollments.value) {
    const enrolledCourse = enrollment.course
    if (!enrolledCourse || !enrolledCourse.timeSlot) continue
    
    const existingSlots = parseTimeSlot(enrolledCourse.timeSlot)
    
    for (const newSlot of newSlots) {
      for (const existingSlot of existingSlots) {
        if (newSlot.day !== existingSlot.day) continue
        if (hasSectionOverlap(newSlot.sections, existingSlot.sections)) {
          if (hasWeekOverlap(newSlot.weeks, existingSlot.weeks)) {
            return true
          }
        }
      }
    }
  }
  
  return false
}

function hasSectionOverlap(a, b) {
  const parseSection = (s) => {
    if (s.includes('-')) {
      const [start, end] = s.split('-').map(Number)
      return { start, end }
    }
    const n = parseInt(s)
    return { start: n, end: n }
  }
  
  const sa = parseSection(a)
  const sb = parseSection(b)
  
  return !(sa.end < sb.start || sb.end < sa.start)
}

function hasWeekOverlap(a, b) {
  if (!a || !b) return true // 如果没有周次信息，假设冲突
  
  const parseWeeks = (s) => {
    const weeks = []
    for (const part of s.split(',')) {
      if (part.includes('-')) {
        const [start, end] = part.split('-').map(Number)
        for (let i = start; i <= end; i++) weeks.push(i)
      } else {
        weeks.push(parseInt(part))
      }
    }
    return weeks
  }
  
  const weeksA = parseWeeks(a)
  const weeksB = parseWeeks(b)
  
  return weeksA.some(w => weeksB.includes(w))
}

async function loadCourses() {
  loading.value = true
  try {
    const [courseData, enrollData, termData] = await Promise.all([
      api.getStudentCourses(search.value || null, semester.value || null, null),
      api.getMyEnrollments(),
      api.getTermSession()
    ])
    courses.value = Array.isArray(courseData) ? courseData : []
    myEnrollments.value = Array.isArray(enrollData) ? enrollData : []
    enrolledIds.value = myEnrollments.value.map(e => e.course?.id || e.courseId).filter(Boolean)
    if (termData) {
      termInfo.value = termData
    }
  } catch (e) {
    msg.value = e.message || '加载失败'
    msgType.value = 'error'
  } finally {
    loading.value = false
  }
}

async function doEnroll(course) {
  msg.value = ''
  
  try {
    await api.enroll(course.id)
    msg.value = `成功选课：${course.name}`
    msgType.value = 'success'
    enrolledIds.value.push(course.id)
    await loadCourses()
  } catch (e) {
    // 检查是否是冲突错误 (HTTP 409)
    if (e.status === 409 || e.data?.conflicts) {
      const errorData = e.data || {}
      conflictDialog.value = {
        show: true,
        newCourse: errorData.newCourse || {
          id: course.id,
          name: course.name,
          code: course.code,
          timeSlot: course.timeSlot
        },
        conflicts: errorData.conflicts || []
      }
    } else {
      msg.value = e.message || '选课失败'
      msgType.value = 'error'
    }
  }
}

async function forceEnroll() {
  const course = conflictDialog.value.newCourse
  conflictDialog.value.show = false
  
  if (!course) return
  
  try {
    await api.enroll(course.id, true) // force = true
    msg.value = `成功选课：${course.name}（已忽略时间冲突）`
    msgType.value = 'success'
    enrolledIds.value.push(course.id)
    await loadCourses()
  } catch (e) {
    msg.value = e.message || '选课失败'
    msgType.value = 'error'
  }
}

async function doDrop(course) {
  if (!confirm(`确定要退课：${course.name} 吗？`)) return
  
  msg.value = ''
  try {
    await api.dropCourse(course.id)
    msg.value = `成功退课：${course.name}`
    msgType.value = 'success'
    enrolledIds.value = enrolledIds.value.filter(id => id !== course.id)
    await loadCourses()
  } catch (e) {
    msg.value = e.message || '退课失败'
    msgType.value = 'error'
  }
}

// 快速退课（从右侧导航栏）
async function quickDrop(course) {
  if (!confirm(`确定要退课：${course.name} 吗？`)) return
  
  msg.value = ''
  try {
    await api.dropCourse(course.id)
    msg.value = `成功退课：${course.name}`
    msgType.value = 'success'
    enrolledIds.value = enrolledIds.value.filter(id => id !== course.id)
    await loadCourses()
  } catch (e) {
    msg.value = e.message || '退课失败'
    msgType.value = 'error'
  }
}

let termPollTimer = null
onMounted(() => {
  loadCourses()
  termPollTimer = setInterval(async () => {
    try { const data = await api.getTermSession(); if (data) termInfo.value = data } catch {}
  }, 5000)
})
onUnmounted(() => clearInterval(termPollTimer))
</script>