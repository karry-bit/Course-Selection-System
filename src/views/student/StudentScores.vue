<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">我的成绩</h2>
    <div v-if="loading" class="text-gray-500">加载中...</div>
    <table v-else-if="scores.length" class="w-full bg-white rounded-xl shadow-sm border overflow-hidden">
      <thead class="bg-gray-50">
        <tr>
          <th class="text-left px-4 py-3 text-sm font-semibold text-gray-600">课程代码</th>
          <th class="text-left px-4 py-3 text-sm font-semibold text-gray-600">课程名称</th>
          <th class="text-center px-4 py-3 text-sm font-semibold text-gray-600">学分</th>
          <th class="text-center px-4 py-3 text-sm font-semibold text-gray-600">成绩</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in scores" :key="s.enrollmentId" class="border-t hover:bg-gray-50 transition">
          <td class="px-4 py-3 text-sm text-gray-600">{{ s.courseCode }}</td>
          <td class="px-4 py-3 text-sm font-medium text-gray-800">{{ s.courseName }}</td>
          <td class="px-4 py-3 text-sm text-center text-gray-600">{{ s.credit }}</td>
          <td class="px-4 py-3 text-sm text-center font-bold" :class="s.score != null ? 'text-indigo-600' : 'text-gray-400'">
            {{ s.score != null ? s.score : '未录入' }}
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="text-gray-400">暂无成绩记录</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'

const scores = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    scores.value = await api.getMyScores()
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
})
</script>