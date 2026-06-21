<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-bold text-gray-800">选课时段管理</h2>
      <button @click="showForm = true; editingItem = null; resetForm()" class="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition">
        + 添加时段
      </button>
    </div>

    <!-- Form dialog -->
    <div v-if="showForm" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
        <h3 class="text-lg font-bold mb-4">{{ editingItem ? '编辑时段' : '添加时段' }}</h3>
        <div class="space-y-3">
          <input v-model="form.name" placeholder="时段名称 (如: 2024秋选课)" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.semester" placeholder="学期 (如: 2024-2025-1)" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="text-xs text-gray-500">开始时间</label>
              <input v-model="form.startTime" type="datetime-local" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500 text-sm" />
            </div>
            <div>
              <label class="text-xs text-gray-500">结束时间</label>
              <input v-model="form.endTime" type="datetime-local" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500 text-sm" />
            </div>
          </div>
          <label class="flex items-center gap-2 text-sm">
            <input v-model="form.isOpen" type="checkbox" class="rounded" />
            立即开放
          </label>
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
    <div v-else-if="sessions.length" class="bg-white rounded-xl shadow-sm border overflow-x-auto">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">名称</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">学期</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">开始</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">结束</th>
            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">状态</th>
            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in sessions" :key="s.id" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 text-sm font-medium text-gray-800">{{ s.name }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ s.semester }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ formatDate(s.startTime) }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ formatDate(s.endTime) }}</td>
            <td class="px-4 py-3 text-center">
              <span :class="s.isOpen ? 'text-green-600 bg-green-50' : 'text-red-500 bg-red-50'" class="px-2 py-0.5 rounded-full text-xs font-medium">
                {{ s.isOpen ? '开放' : '关闭' }}
              </span>
            </td>
            <td class="px-4 py-3 text-center space-x-2">
              <button @click="startEdit(s)" class="text-indigo-600 hover:underline text-sm">编辑</button>
              <button @click="remove(s.id)" class="text-red-500 hover:underline text-sm">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="text-gray-400">暂无选课时段</p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import api from '../../api'

const sessions = ref([])
const loading = ref(false)
const showForm = ref(false)
const editingItem = ref(null)
const saving = ref(false)
const msg = ref('')
const msgType = ref('success')

const form = reactive({ name: '', semester: '', startTime: '', endTime: '', isOpen: false })

function formatDate(d) {
  if (!d) return '-'
  return new Date(d).toLocaleString('zh-CN', { hour12: false })
}

function toLocalISO(d) {
  if (!d) return ''
  const dt = new Date(d)
  // Return YYYY-MM-DDTHH:mm format for datetime-local input
  const pad = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())}T${pad(dt.getHours())}:${pad(dt.getMinutes())}`
}

function resetForm() {
  form.name = ''
  form.semester = ''
  form.startTime = ''
  form.endTime = ''
  form.isOpen = false
  msg.value = ''
}

async function load() {
  loading.value = true
  try {
    sessions.value = await api.getAdminTermSessions()
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function startEdit(item) {
  editingItem.value = item
  form.name = item.name || ''
  form.semester = item.semester || ''
  form.startTime = toLocalISO(item.startTime)
  form.endTime = toLocalISO(item.endTime)
  form.isOpen = !!item.isOpen
  showForm.value = true
}

async function save() {
  if (!form.name || !form.semester) {
    msg.value = '名称和学期为必填'
    msgType.value = 'error'
    return
  }
  saving.value = true
  msg.value = ''
  try {
    const payload = { ...form }
    if (editingItem.value) {
      await api.updateTermSession(editingItem.value.id, payload)
    } else {
      await api.createTermSession(payload)
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
  if (!confirm('确定删除该时段？')) return
  try {
    await api.deleteTermSession(id)
    await load()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

let pollTimer = null
onMounted(() => { load(); pollTimer = setInterval(load, 5000); })
onUnmounted(() => clearInterval(pollTimer))
</script>