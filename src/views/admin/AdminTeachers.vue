<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-bold text-gray-800">教师管理</h2>
      <div class="flex gap-2">
        <button @click="downloadTeacherTemplate" class="px-4 py-2 bg-gray-600 text-white text-sm rounded-lg hover:bg-gray-700 transition">
          下载模板
        </button>
        <label class="px-4 py-2 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700 transition cursor-pointer">
          <input type="file" accept=".xlsx,.xls,.csv" @change="handleTeacherImport" class="hidden" />
          导入教师
        </label>
        <button @click="showForm = true; editingItem = null; resetForm()" class="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition">
          + 添加教师
        </button>
      </div>
    </div>

    <div v-if="showForm" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
        <h3 class="text-lg font-bold mb-4">{{ editingItem ? '编辑教师' : '添加教师' }}</h3>
        <div class="space-y-3">
          <input v-model="form.name" placeholder="姓名" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.teacherNo" placeholder="工号" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.phone" placeholder="电话 (可选)" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.email" placeholder="邮箱 (可选)" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
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
    <div v-else-if="teachers.length" class="bg-white rounded-xl shadow-sm border overflow-x-auto">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">工号</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">姓名</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">电话</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">邮箱</th>
            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in teachers" :key="t.id" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 text-sm text-gray-600">{{ t.teacherNo }}</td>
            <td class="px-4 py-3 text-sm font-medium text-gray-800">{{ t.name }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ t.phone || '-' }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ t.email || '-' }}</td>
            <td class="px-4 py-3 text-center space-x-2">
              <button @click="startEdit(t)" class="text-indigo-600 hover:underline text-sm">编辑</button>
              <button @click="resetPwd(t.id)" class="text-orange-600 hover:underline text-sm">重置密码</button>
              <button @click="remove(t.id)" class="text-red-500 hover:underline text-sm">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="text-gray-400">暂无教师</p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../../api'

const teachers = ref([])
const loading = ref(false)
const showForm = ref(false)
const editingItem = ref(null)
const saving = ref(false)
const msg = ref('')
const msgType = ref('success')

const form = reactive({ name: '', teacherNo: '', phone: '', email: '' })

function resetForm() {
  form.name = ''
  form.teacherNo = ''
  form.phone = ''
  form.email = ''
  msg.value = ''
}

async function load() {
  loading.value = true
  try {
    teachers.value = await api.getAdminTeachers()
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function startEdit(item) {
  editingItem.value = item
  form.name = item.name || ''
  form.teacherNo = item.teacherNo || ''
  form.phone = item.phone || ''
  form.email = item.email || ''
  showForm.value = true
}

async function save() {
  if (!form.name || !form.teacherNo) {
    msg.value = '姓名和工号为必填'
    msgType.value = 'error'
    return
  }
  saving.value = true
  msg.value = ''
  try {
    if (editingItem.value) {
      await api.updateTeacher(editingItem.value.id, form)
    } else {
      await api.createTeacher(form)
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
  if (!confirm('确定删除该教师？')) return
  try {
    await api.deleteTeacher(id)
    await load()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

async function resetPwd(id) {
  if (!confirm('确定将该教师密码重置为默认密码 123456？')) return
  try {
    await api.resetTeacherPassword(id)
    alert('密码已重置为 123456')
    await load()
  } catch (e) {
    alert(e.message || '重置失败')
  }
}

async function downloadTeacherTemplate() {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/admin/teachers/import/template', {
      headers: { Authorization: `Bearer ${token}` }
    })
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'teacher_import_template.xlsx'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  } catch (e) {
    alert('下载失败: ' + (e.message || '未知错误'))
  }
}

async function handleTeacherImport(event) {
  const file = event.target.files?.[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const result = await api.importTeachers(formData)
    alert(`导入完成！\n成功: ${result.success} 条\n失败: ${result.fail} 条\n初始密码: ${result.defaultPassword}`)
    await load()
  } catch (e) {
    alert('导入失败: ' + (e.message || '未知错误'))
  }
  event.target.value = ''
}

onMounted(load)
</script>