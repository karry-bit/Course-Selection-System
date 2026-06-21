<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">我的课表</h2>
    <div v-if="loading" class="text-gray-500">加载中...</div>
    <div v-else>
      <!-- 选课时段未结束提示 -->
      <div v-if="!canShowCourses" class="mb-4 p-4 bg-amber-50 border border-amber-200 rounded-xl text-center">
        <svg class="w-10 h-10 mx-auto text-amber-500 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <p class="text-amber-700 font-medium">课表将在选课时段结束后开放</p>
        <p class="text-amber-600 text-sm mt-1">{{ termSessionMessage }}</p>
      </div>
      <template v-else>
        <!-- 冲突提示 -->
        <div v-if="hasConflict" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl flex items-center gap-2">
          <svg class="w-5 h-5 text-red-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <span class="text-red-700 text-sm">您有课程时间冲突，请注意查看课表中的红色边框标记</span>
        </div>
        <!-- 表头 -->
        <div class="grid grid-cols-7 gap-2 mb-2">
          <div v-for="d in days" :key="d" class="bg-indigo-100 text-indigo-700 font-bold text-center py-2 rounded">
            星期{{ d }}
          </div>
        </div>
        <!-- 课表网格 -->
        <div class="grid grid-cols-7 gap-2">
          <div
            v-for="(slot, idx) in grid"
            :key="idx"
            :class="[
              'p-2 rounded text-sm min-h-16 flex flex-col items-center justify-center',
              slot.courses.length === 0 ? 'bg-gray-50' : '',
              slot.courses.length > 1 ? 'border-2 border-red-400 bg-red-50' : ''
            ]"
          >
            <!-- 无课程 -->
            <template v-if="slot.courses.length === 0">
              <span class="text-gray-300">-</span>
            </template>
            <!-- 有课程（单个或多个） -->
            <template v-else>
              <div
                v-for="course in slot.courses"
                :key="course.id"
                :style="getCourseStyle(course.id)"
                class="w-full p-1.5 rounded mb-1 last:mb-0 text-center"
              >
                <div class="font-bold text-xs truncate">{{ course.name }}</div>
                <div class="text-xs opacity-80 truncate">{{ course.place || '-' }}</div>
              </div>
            </template>
          </div>
        </div>
        <!-- 图例 -->
        <div v-if="courseLegend.length > 0" class="mt-6">
          <h3 class="text-sm font-medium text-gray-600 mb-2">课程图例</h3>
          <div class="flex flex-wrap gap-2">
            <div
              v-for="legend in courseLegend"
              :key="legend.id"
              class="flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-medium"
              :style="{ backgroundColor: legend.bgColor, color: legend.textColor }"
            >
              <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: legend.textColor }"></span>
              {{ legend.name }}
            </div>
          </div>
        </div>
      </template>
    </div>
    <p v-if="noData && canShowCourses" class="text-gray-400 mt-4">暂无已选课程</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../../api'

const loading = ref(false)
const enrollments = ref([])
const noData = ref(false)
const termSession = ref(null)

// 预设的颜色方案（浅色背景 + 深色文字）
const colorPalette = [
  { bg: '#DBEAFE', text: '#1D4ED8' }, // 蓝色
  { bg: '#D1FAE5', text: '#047857' }, // 绿色
  { bg: '#FEE2E2', text: '#B91C1C' }, // 红色
  { bg: '#FEF3C7', text: '#B45309' }, // 橙色
  { bg: '#E9D5FF', text: '#7E22CE' }, // 紫色
  { bg: '#FCE7F3', text: '#BE185D' }, // 粉色
  { bg: '#CFFAFE', text: '#0E7490' }, // 青色
  { bg: '#FEF9C3', text: '#A16207' }, // 黄色
  { bg: '#F3E8FF', text: '#6B21A8' }, // 紫罗兰
  { bg: '#DCFCE7', text: '#15803D' }, // 翠绿
  { bg: '#FFEDD5', text: '#C2410C' }, // 橙红
  { bg: '#DBEAFE', text: '#1E40AF' }, // 深蓝
]

// 课程ID到颜色的映射
const courseColorMap = ref({})

// 获取课程的颜色样式
function getCourseStyle(courseId) {
  const color = courseColorMap.value[courseId]
  if (color) {
    return {
      backgroundColor: color.bg,
      color: color.text
    }
  }
  return {
    backgroundColor: '#F3F4F6',
    color: '#6B7280'
  }
}

// 根据课程ID获取或分配颜色
function getColorForCourse(courseId) {
  if (!courseColorMap.value[courseId]) {
    const usedCount = Object.keys(courseColorMap.value).length
    const colorIndex = usedCount % colorPalette.length
    courseColorMap.value[courseId] = colorPalette[colorIndex]
  }
  return courseColorMap.value[courseId]
}

// 课程图例
const courseLegend = computed(() => {
  const uniqueCourses = []
  const seenIds = new Set()
  for (const enrollment of enrollments.value) {
    if (enrollment.course && !seenIds.has(enrollment.course.id)) {
      seenIds.add(enrollment.course.id)
      const color = getColorForCourse(enrollment.course.id)
      uniqueCourses.push({
        id: enrollment.course.id,
        name: enrollment.course.name,
        bgColor: color.bg,
        textColor: color.text
      })
    }
  }
  return uniqueCourses
})

// 判断选课时段是否已结束（可以显示课表）
const canShowCourses = computed(() => {
  if (!termSession.value) return true // 如果没有选课时段数据，默认显示
  if (!termSession.value.isOpen) return true // 选课时段未开放，显示课表
  
  // 选课时段正在开放，检查是否已过结束时间
  const endTime = termSession.value.endTime
  if (!endTime) return true // 没有结束时间，默认显示
  
  const now = new Date()
  const end = new Date(endTime)
  return now > end // 当前时间超过结束时间才显示
})

// 选课时段提示信息
const termSessionMessage = computed(() => {
  if (!termSession.value) return ''
  if (termSession.value.isOpen && termSession.value.endTime) {
    const endDate = new Date(termSession.value.endTime)
    const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' }
    return `选课时段 "${termSession.value.name}" 结束时间：${endDate.toLocaleString('zh-CN', options)}`
  }
  return ''
})

const days = ['一', '二', '三', '四', '五', '六', '日']
const periods = [1, 3, 5, 7, 9] // start of period pairs

// 解析时间格式： "周一12节" -> {day: '一', sections: [1,2]}, "周三3-4节" -> {day: '三', sections: [3,4]}, "周三910节" -> {day: '三', sections: [9,10]}
function parseTimeSlot(timeSlot) {
  if (!timeSlot) return []
  const dayMap = { '周一': '一', '周二': '二', '周三': '三', '周四': '四', '周五': '五', '周六': '六', '周日': '日' }
  const result = []
  const parts = timeSlot.split(/[;；]/)
  for (const part of parts) {
    const m = part.trim().match(/(周[一二三四五六日])(\d+)(?:-(\d+))?节/)
    if (m) {
      const day = dayMap[m[1]] || m[1]
      // 修正节次解析：将连续数字拆分为单个节次
      // "12" -> [1,2], "34" -> [3,4], "910" -> [9,10], "1" -> [1]
      const sectionStr = m[2]
      let sections = []
      for (let i = 0; i < sectionStr.length; i++) {
        sections.push(parseInt(sectionStr[i]))
      }
      result.push({ day, sections })
    }
  }
  return result
}

// 检查指定节次是否在课程的时间段内
function hasSection(sections, periodStart) {
  return sections.some(s => s >= periodStart && s <= periodStart + 1)
}

// 判断是否有冲突
const hasConflict = computed(() => {
  return grid.value.some(slot => slot.courses.length > 1)
})

// 课表网格（支持显示多个课程）
const grid = computed(() => {
  const slots = []
  for (const p of periods) {
    for (const d of days) {
      // 查找该时间段的所有课程（用于显示冲突）
      const courses = []
      for (const e of enrollments.value) {
        const t = e.course?.timeSlot
        if (!t) continue
        const parsed = parseTimeSlot(t)
        // 检查该课程是否在当前时间段有课
        const hasThisPeriod = parsed.some(slot => slot.day === d && hasSection(slot.sections, p))
        if (hasThisPeriod) {
          courses.push(e.course)
        }
      }
      slots.push({ day: d, period: `${p}-${p + 1}`, courses })
    }
  }
  return slots
})

onMounted(async () => {
  loading.value = true
  try {
    // 并行获取选课记录和选课时段信息
    const [data, termData] = await Promise.all([
      api.getMyEnrollments(),
      api.getTermSession()
    ])
    
    enrollments.value = Array.isArray(data) ? data.filter(e => e.status === 'ENROLLED' || !e.status) : []
    termSession.value = termData
    
    noData.value = enrollments.value.length === 0
  } catch (e) {
    console.error('[课表] 获取选课记录失败:', e)
    noData.value = true
  } finally {
    loading.value = false
  }
})
</script>