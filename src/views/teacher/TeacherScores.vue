<template>
  <div>
    <h2 class="text-xl font-bold text-gray-800 mb-4">成绩录入</h2>
    <div v-if="loading" class="text-gray-500">加载中...</div>
    <div v-else-if="courses.length">
      <!-- Course selector -->
      <div class="mb-4">
        <select v-model="selectedCourseId" @change="loadStudents" class="px-4 py-2 border rounded-lg outline-none">
          <option :value="null">选择课程...</option>
          <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }} ({{ c.code }})</option>
        </select>
      </div>

      <!-- Student table -->
      <div v-if="selectedCourseId && students.length" class="bg-white rounded-xl shadow-sm border overflow-hidden">
        <table class="w-full">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">学号</th>
              <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">姓名</th>
              <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">成绩</th>
              <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in students" :key="s.id" class="border-t hover:bg-gray-50">
              <td class="px-4 py-3 text-sm text-gray-600">{{ s.student?.studentNo || '-' }}</td>
              <td class="px-4 py-3 text-sm font-medium text-gray-800">
                {{ s.student?.name || '未知' }}
                <span v-if="s.student?.className" class="text-gray-400 ml-1">({{ s.student.className }})</span>
              </td>
              <td class="px-4 py-3 text-center">
                <input
                  v-model.number="s._score"
                  type="number"
                  min="0"
                  max="100"
                  class="w-20 px-2 py-1 border rounded text-center text-sm outline-none focus:ring-2 focus:ring-indigo-500"
                  @keyup.enter="saveOne(s)"
                />
              </td>
              <td class="px-4 py-3 text-center">
                <button
                  @click="saveOne(s)"
                  :disabled="s._saving"
                  class="px-3 py-1 bg-indigo-600 text-white text-xs rounded hover:bg-indigo-700 transition disabled:opacity-50"
                >
                  {{ s._saving ? '...' : '保存' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="px-4 py-3 border-t">
          <button @click="saveAll" :disabled="batchSaving" class="px-4 py-2 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700 transition disabled:opacity-50">
            {{ batchSaving ? '保存中...' : '一键保存全部' }}
          </button>
          <span v-if="msg" :class="msgType === 'error' ? 'text-red-500' : 'text-green-600'" class="ml-3 text-sm">{{ msg }}</span>
        </div>
      </div>
      <p v-else-if="selectedCourseId" class="text-gray-400 mt-4">该课程暂无学生选课</p>
    </div>
    <p v-else class="text-gray-400">暂无教授课程</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'

const courses = ref([])
const students = ref([])
const selectedCourseId = ref(null)
const loading = ref(false)
const batchSaving = ref(false)
const msg = ref('')
const msgType = ref('success')

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

async function loadStudents() {
  if (!selectedCourseId.value) { students.value = []; return }
  try {
    const data = await api.getCourseStudents(selectedCourseId.value)
    students.value = (Array.isArray(data) ? data : []).map(s => ({
      ...s,
      _score: s.score,
      _saving: false
    }))
  } catch (e) {
    students.value = []
  }
}

async function saveOne(s) {
  s._saving = true
  msg.value = ''
  try {
    await api.setScore(s.id, s._score)
    s.score = s._score
    msg.value = '已保存'
    msgType.value = 'success'
    setTimeout(() => { msg.value = '' }, 2000)
  } catch (e) {
    msg.value = e.message || '保存失败'
    msgType.value = 'error'
  } finally {
    s._saving = false
  }
}

async function saveAll() {
  batchSaving.value = true
  msg.value = ''
  try {
    const payload = students.value.map(s => ({ enrollmentId: s.id, score: s._score }))
    await api.batchSetScore(payload)
    students.value.forEach(s => { s.score = s._score })
    msg.value = '全部成绩已保存'
    msgType.value = 'success'
    setTimeout(() => { msg.value = '' }, 3000)
  } catch (e) {
    msg.value = e.message || '批量保存失败'
    msgType.value = 'error'
  } finally {
    batchSaving.value = false
  }
}
</script>