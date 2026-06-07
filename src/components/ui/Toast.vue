<template>
  <div class="fixed right-4 top-4 z-50 space-y-2">
    <div v-for="(t, i) in toasts" :key="i" class="bg-white shadow-sm rounded-md p-3">{{ t }}</div>
  </div>
</template>

<script>
export default {
  name: 'UiToast',
  data() { return { toasts: [] } },
  methods: {
    push(msg, timeout = 3000) {
      this.toasts.push(msg)
      const idx = this.toasts.length - 1
      setTimeout(() => { this.toasts.splice(idx, 1) }, timeout)
    }
  }
}
</script>
