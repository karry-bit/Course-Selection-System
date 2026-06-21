<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-bold text-gray-800">班级管理</h2>
      <button @click="showForm = true; editingItem = null; resetForm()" class="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition">
        + 添加班级
      </button>
    </div>

    <div v-if="showForm" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-sm">
        <h3 class="text-lg font-bold mb-4">{{ editingItem ? '编辑班级' : '添加班级' }}</h3>
        <div class="space-y-3">
          <input v-model="form.name" placeholder="班级名称" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.grade" placeholder="年级 (如: 2024)" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.department" placeholder="院系/专业 (可选)" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
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
    <div v-else-if="classes.length" class="bg-white rounded-xl shadow-sm border overflow-x-auto">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">班级名称</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">年级</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">院系/专业</th>
            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in classes" :key="c.id" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 text-sm font-medium text-gray-800">{{ c.name }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ c.grade || '-' }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ c.department || '-' }}</td>
            <td class="px-4 py-3 text-center space-x-2">
              <button @click="startEdit(c)" class="text-indigo-600 hover:underline text-sm">编辑</button>
              <button @click="remove(c.id)" class="text-red-500 hover:underline text-sm">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="text-gray-400">暂无班级</p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../../api'

const classes = ref([])
const loading = ref(false)
const showForm = ref(false)
const editingItem = ref(null)
const saving = ref(false)
const msg = ref('')
const msgType = ref('success')

const form = reactive({ name: '', grade: '', department: '' })

function resetForm() {
  form.name = ''
  form.grade = ''
  form.department = ''
  msg.value = ''
}

async function load() {
  loading.value = true
  try {
    classes.value = await api.getAdminClasses()
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function startEdit(item) {
  editingItem.value = item
  form.name = item.name || ''
  form.grade = item.grade || ''
  form.department = item.department || ''
  showForm.value = true
}

async function save() {
  if (!form.name) {
    msg.value = '班级名称为必填'
    msgType.value = 'error'
    return
  }
  saving.value = true
  msg.value = ''
  try {
    if (editingItem.value) {
      await api.updateClass(editingItem.value.id, form)
    } else {
      await api.createClass(form)
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
  if (!confirm('确定删除该班级？')) return
  try {
    await api.deleteClass(id)
    await load()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

onMounted(load)
</script>