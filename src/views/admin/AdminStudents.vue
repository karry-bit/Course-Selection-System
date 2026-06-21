<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-xl font-bold text-gray-800">学生管理</h2>
      <div class="flex gap-2">
        <button @click="downloadStudentTemplate" class="px-4 py-2 bg-gray-600 text-white text-sm rounded-lg hover:bg-gray-700 transition">
          下载模板
        </button>
        <label class="px-4 py-2 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700 transition cursor-pointer">
          <input type="file" accept=".xlsx,.xls,.csv" @change="handleStudentImport" class="hidden" />
          导入学生
        </label>
        <button @click="showForm = true; editingItem = null; resetForm()" class="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition">
          + 添加学生
        </button>
      </div>
    </div>

    <!-- Create / Edit Dialog -->
    <div v-if="showForm" class="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
        <h3 class="text-lg font-bold mb-4">{{ editingItem ? '编辑学生' : '添加学生' }}</h3>
        <div class="space-y-3">
          <input v-model="form.name" placeholder="姓名" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <input v-model="form.studentNo" placeholder="学号" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500" />
          <select v-model="form.classId" class="w-full px-3 py-2 border rounded outline-none focus:ring-2 focus:ring-indigo-500">
            <option :value="null" disabled>请选择班级 *</option>
            <option v-for="c in classes" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
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

    <!-- Table -->
    <div v-if="loading" class="text-gray-500">加载中...</div>
    <div v-else-if="students.length" class="bg-white rounded-xl shadow-sm border overflow-x-auto">
      <table class="w-full">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">学号</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">姓名</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">班级</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">电话</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-600">邮箱</th>
            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in students" :key="s.id" class="border-t hover:bg-gray-50">
            <td class="px-4 py-3 text-sm text-gray-600">{{ s.studentNo }}</td>
            <td class="px-4 py-3 text-sm font-medium text-gray-800">{{ s.name }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ s.classInfo?.name || s.clazz || '-' }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ s.phone || '-' }}</td>
            <td class="px-4 py-3 text-sm text-gray-600">{{ s.email || '-' }}</td>
            <td class="px-4 py-3 text-center space-x-2">
              <button @click="startEdit(s)" class="text-indigo-600 hover:underline text-sm">编辑</button>
              <button @click="resetPwd(s.id)" class="text-orange-600 hover:underline text-sm">重置密码</button>
              <button @click="remove(s.id)" class="text-red-500 hover:underline text-sm">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="text-gray-400">暂无学生</p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../../api'

const students = ref([])
const classes = ref([])
const loading = ref(false)
const showForm = ref(false)
const editingItem = ref(null)
const saving = ref(false)
const msg = ref('')
const msgType = ref('success')

const form = reactive({ name: '', studentNo: '', classId: null, phone: '', email: '' })

function resetForm() {
  form.name = ''
  form.studentNo = ''
  form.classId = null
  form.phone = ''
  form.email = ''
  msg.value = ''
}

async function load() {
  loading.value = true
  try {
    const [s, c] = await Promise.all([
      api.getAdminStudents(),
      api.getAdminClasses()
    ])
    students.value = Array.isArray(s) ? s : []
    classes.value = Array.isArray(c) ? c : []
  } catch (e) {
    // ignore
  } finally {
    loading.value = false
  }
}

function startEdit(item) {
  editingItem.value = item
  form.name = item.name || ''
  form.studentNo = item.studentNo || ''
  // 优先使用classInfo.id，否则通过className查找
  form.classId = item.classInfo?.id || classes.value.find(c => c.name === (item.classInfo?.name || item.clazz))?.id || null
  form.phone = item.phone || ''
  form.email = item.email || ''
  showForm.value = true
}

async function save() {
  if (!form.name || !form.studentNo) {
    msg.value = '姓名和学号为必填'
    msgType.value = 'error'
    return
  }
  if (!form.classId) {
    msg.value = '请选择班级'
    msgType.value = 'error'
    return
  }
  saving.value = true
  msg.value = ''
  try {
    const submitData = {
      name: form.name,
      studentNo: form.studentNo,
      classId: form.classId,
      phone: form.phone,
      email: form.email
    }
    if (editingItem.value) {
      await api.updateStudent(editingItem.value.id, submitData)
    } else {
      await api.createStudent(submitData)
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
  if (!confirm('确定删除该学生？')) return
  try {
    await api.deleteStudent(id)
    await load()
  } catch (e) {
    alert(e.message || '删除失败')
  }
}

async function resetPwd(id) {
  if (!confirm('确定将该学生密码重置为默认密码 123456？')) return
  try {
    await api.resetStudentPassword(id)
    alert('密码已重置为 123456')
    await load()
  } catch (e) {
    alert(e.message || '重置失败')
  }
}

async function downloadStudentTemplate() {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/admin/students/import/template', {
      headers: { Authorization: `Bearer ${token}` }
    })
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'student_import_template.xlsx'
    document.body.appendChild(a)
    a.click()
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
  } catch (e) {
    alert('下载失败: ' + (e.message || '未知错误'))
  }
}

async function handleStudentImport(event) {
  const file = event.target.files?.[0]
  if (!file) return
  
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const result = await api.importStudents(formData)
    alert(`导入完成！\n成功: ${result.success} 条\n失败: ${result.fail} 条\n初始密码: ${result.defaultPassword}`)
    await load()
  } catch (e) {
    alert('导入失败: ' + (e.message || '未知错误'))
  }
  event.target.value = ''
}

onMounted(load)
</script>