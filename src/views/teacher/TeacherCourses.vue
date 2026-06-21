<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">我的教授课程</h2>
    <div v-if="loading" class="text-gray-500">加载中...</div>
    <div v-else-if="courses.length" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="c in courses"
        :key="c.id"
        class="bg-white rounded-xl shadow-sm border p-5"
      >
        <h3 class="font-bold text-lg text-gray-800">{{ c.name }}</h3>
        <p class="text-xs text-gray-400 mt-1">{{ c.code }} · {{ c.semester }}</p>
        <div class="text-sm text-gray-600 mt-3 space-y-1">
          <p>时间：{{ c.timeSlot || '待定' }} · 地点：{{ c.place || '待定' }}</p>
          <p>已选：{{ c.enrollmentCount || 0 }} / {{ c.capacity }} 人</p>
        </div>
      </div>
    </div>
    <p v-else class="text-gray-400">暂无教授课程</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'

const courses = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    courses.value = await api.getTeacherCourses()
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
})
</script>