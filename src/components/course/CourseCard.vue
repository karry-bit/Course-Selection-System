<template>
  <div class="card">
    <div class="flex items-start justify-between">
      <div>
        <div class="text-md font-semibold">{{ course.name }}</div>
        <div class="text-sm text-gray-500">教师：{{ course.teacher }} · {{ course.time }} · 地点：{{ course.place }}</div>
      </div>
      <div class="text-right">
        <div class="text-sm text-gray-500">剩余名额</div>
        <div class="text-lg font-bold">{{ course.remain }}</div>
      </div>
    </div>
    <div class="mt-3 flex items-center justify-between">
      <div class="flex gap-2 items-center">
        <span class="badge">{{ course.type }}</span>
        <span class="text-sm text-gray-500">学分：{{ course.credit }}</span>
      </div>
      <div class="flex gap-2">
        <button class="btn" @click="select">选课</button>
        <button class="px-3 py-1.5 rounded-md border border-gray-200 text-sm" @click="$emit('detail', course)">详情</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CourseCard',
  props: { course: { type: Object, required: true } },
  methods: {
    select() { this.$emit('select', this.course) }
  }
}
</script>
