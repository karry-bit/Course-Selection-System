<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-bold text-gray-800">📖 课程管理</h2>
      <button @click="showForm = true; editingItem = null; resetForm()" class="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition">
        + 添加课程
      </button>
    </div>

    <div v-if="showForm" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg">
        <h3 class="text-lg font-bold mb-4">{{ editingItem ? '编辑课程' : '添加课程' }}</h3>
        <div class="grid grid-cols-2 gap-3">
          <input v-model="form.name" placeholder="课程名称" class="px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.code" placeholder="课程代码" class="px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.credit" type="number" step="0.5" placeholder="学分" class="px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.capacity" type="number" placeholder="容量" class="px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.timeSlot" placeholder="时间 (如: 一 1-2)" class="px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.place" placeholder="地点" class="px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.semester" placeholder="学期 (如: 2024-2025-1)" class="px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500 col-span-2" />
          <select v-model="form.teacherId" class="px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500 col-span-2">
            <option :value="null">选择授课教师</option>
            <option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.name }} ({{ t.teacherNo }})</option>
          </select>
          <textarea v-model="form.description" placeholder="课程描述 (可选)" rows="2" class="px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500 col-span-2"></textarea>
        </div>
        <p v-if="msg" :class="msgType === 'error' ? 'text-red-500' : 'text-green-600'" class="text-sm mt-2">{{ msg }}</p>
        <div class="flex justify-end gap-2 mt-4">
          <button @click="showForm = false" class="px-4 py-2 bg-gray-100 text-gray-600 text-sm rounded-lg hover:bg-gray-200">取消</button>
          <button @click="save" :disabled="saving" class="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 disabled:opacity-50">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="loading" class="text-gray-500">加载中...</div>
    <div v-else-if="courses.length" class="bg-white rounded-xl shadow-sm border overflow-x-auto">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">代码</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">名称</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">教师</th>
            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">学分</th>
            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">容量</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">学期</th>
            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in courses" :key="c.id" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 text-sm text-gray-600">{{ c.code }}</td>
            <td class="px-4 py-3 text-sm font-medium text-gray-800">{{ c.name }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ c.teacher?.name || '-' }}</td>
            <td class="px-4 py-3 text-sm text-center text-gray-600">{{ c.credit }}</td>
            <td class="px-4 py-3 text-sm text-center text-gray-600">{{ c.capacity }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ c.semester }}</td>
            <td class="px-4 py-3 text-center space-x-2">
              <button @click="startEdit(c)" class="text-indigo-600 hover:underline text-sm">编辑</button>
              <button @click="remove(c.id)" class="text-red-500 hover:underline text-sm">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="text-gray-400">暂无课程</p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../../api'

const courses = ref([])
const teachers = ref([])
const loading = ref(false)
const showForm = ref(false)
const editingItem = ref(null)
const saving = ref(false)
const msg = ref('')
const msgType = ref('success')

const form = reactive({ name: '', code: '', credit: 0, capacity: 0, timeSlot: '', place: '', semester: '', teacherId: null, description: '' })

function resetForm() {
  form.name = ''
  form.code = ''
  form.credit = 0
  form.capacity = 0
  form.timeSlot = ''
  form.place = ''
  form.semester = ''
  form.teacherId = null
  form.description = ''
  msg.value = ''
  // 确保教师列表已加载
  if (teachers.value.length === 0) {
    loadTeachers()
  }
}

async function loadTeachers() {
  try {
    const t = await api.getAdminTeachers()
    teachers.value = Array.isArray(t) ? t : []
  } catch (e) {
    console.error('加载教师列表失败:', e)
  }
}

async function load() {
  loading.value = true
  try {
    const [c, t] = await Promise.all([
      api.getAdminCourses(),
      api.getAdminTeachers()
    ])
    courses.value = Array.isArray(c) ? c : []
    teachers.value = Array.isArray(t) ? t : []
  } catch (e) {
    console.error('加载失败:', e)
    alert('加载失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

function startEdit(item) {
  editingItem.value = item
  form.name = item.name || ''
  form.code = item.code || ''
  form.credit = item.credit || 0
  form.capacity = item.capacity || 0
  form.timeSlot = item.timeSlot || ''
  form.place = item.place || ''
  form.semester = item.semester || ''
  form.teacherId = item.teacherId || item.teacher?.id || null
  form.description = item.description || ''
  showForm.value = true
}

async function save() {
  if (!form.name || !form.code) {
    msg.value = '课程名称和代码为必填'
    msgType.value = 'error'
    return
  }
  saving.value = true
  msg.value = ''
  try {
    if (editingItem.value) {
      await api.updateCourse(editingItem.value.id, form)
    } else {
      await api.createCourse(form)
    }
    showForm.value = false
    editingItem.value = null
    await load()
  } catch (e) {
    msg.value = e.message || '保存失败'
    msgType.value = 'error'
  } finally {
    saving.value = false
  }
}

async function remove(id) {
  if (!confirm('确定删除该课程？')) return
  try {
    await api.deleteCourse(id)
    await load()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

onMounted(load)
</script>