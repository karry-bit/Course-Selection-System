<template>
  <div class="min-h-screen bg-gray-100">
    <header class="bg-white shadow-sm">
      <div class="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="text-2xl font-semibold">UniCourse</div>
          <div class="text-sm text-gray-500">大学生选课系统 Demo</div>
        </div>
        <div class="flex items-center gap-3">
          <input placeholder="搜索课程/教师" class="px-3 py-1 rounded-md border border-gray-200" />
          <button class="btn">我的选课</button>
        </div>
      </div>
    </header>

    <main class="max-w-6xl mx-auto px-4 py-6 grid grid-cols-1 md:grid-cols-4 gap-6">
      <section class="md:col-span-3 space-y-4">
        <div class="card flex items-center justify-between">
          <div>
            <div class="text-lg font-medium">课程浏览</div>
            <div class="text-sm text-gray-500">筛选、搜索并快速选课</div>
          </div>
          <div class="flex items-center gap-2">
            <span class="badge">选课开放</span>
            <button class="btn">新增课程（管理员）</button>
          </div>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <CourseCard v-for="c in courses" :key="c.id" :course="c" @select="onSelect" />
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div class="card">
            <div class="font-medium mb-2">我的课程表</div>
            <div class="text-sm text-gray-500">周一：高等数学 · 周二：程序设计基础 · ...</div>
          </div>

          <div class="card">
            <div class="font-medium mb-2">成绩快速查看</div>
            <div class="text-sm text-gray-500">高等数学：88 · 程序设计基础：92 · ...</div>
          </div>
        </div>
      </section>

      <aside class="space-y-4">
        <SideRail />
      </aside>
    </main>
    <UiToast ref="toast" />

    <UiDialog v-model="showConflictDialog" title="选课冲突">
      <div class="text-sm text-gray-600">{{ conflictMessage }}</div>
      <template #actions>
        <button class="px-3 py-1.5 rounded-md border" @click="showConflictDialog = false">取消</button>
        <button class="btn" @click="showConflictDialog = false">查看替代课程</button>
      </template>
    </UiDialog>
  </div>
</template>

<script>
import CourseCard from './components/course/CourseCard.vue'
import SideRail from './components/layout/SideRail.vue'
import UiToast from './components/ui/Toast.vue'
import UiDialog from './components/ui/Dialog.vue'
import { enroll } from './api'

export default {
  name: 'App',
  components: { CourseCard, SideRail, UiToast, UiDialog },
  data() {
    return {
      courses: [
        { id: 1, code: 'C001', name: '高等数学', teacher: '张老师', timeSlot: 'MON_09_11', time: '周一 09:00-11:00', place: 'A101', credit: 3, remain: 8, type: '必修' },
        { id: 2, code: 'C002', name: '程序设计基础', teacher: '李老师', timeSlot: 'TUE_14_16', time: '周二 14:00-16:00', place: 'B201', credit: 2, remain: 2, type: '选修' }
      ],
      showConflictDialog: false,
      conflictMessage: ''
    }
  },
  methods: {
    async onSelect(course) {
      try {
        // demo 使用 studentId=1，生产应从登录态获取
        await enroll(1, course.id)
        // 调用 toast 组件显示成功
        if (this.$refs.toast && this.$refs.toast.push) this.$refs.toast.push('选课成功')
      } catch (err) {
        const message = err && err.error ? err.error : (err && err.message) ? err.message : '选课失败'
        if (message.toLowerCase().includes('conflict')) {
          this.conflictMessage = message
          this.showConflictDialog = true
        } else {
          if (this.$refs.toast && this.$refs.toast.push) this.$refs.toast.push(message)
        }
      }
    }
  }
}
</script>

<style>
/* Scoped styles are handled in components */
</style>
