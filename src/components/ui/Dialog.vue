<template>
  <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center" @keydown.esc="close">
    <div class="absolute inset-0 bg-black/40" @click="close"></div>
    <div class="bg-white rounded-lg p-6 z-10 w-11/12 max-w-md" role="dialog" aria-modal="true" :aria-label="title">
      <div class="flex justify-between items-center mb-3">
        <div class="font-medium text-lg">{{ title }}</div>
        <button class="text-gray-500 hover:text-gray-700" @click="close" aria-label="关闭">&times;</button>
      </div>
      <div>
        <slot />
      </div>
      <div v-if="$slots.actions" class="mt-4 flex justify-end gap-2">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UiDialog',
  props: {
    modelValue: { type: Boolean, required: true },
    title: { type: String, default: '' }
  },
  emits: ['update:modelValue'],
  mounted() {
    document.addEventListener('keydown', this.onKey)
  },
  beforeUnmount() {
    document.removeEventListener('keydown', this.onKey)
  },
  methods: {
    close() { this.$emit('update:modelValue', false) },
    onKey(e) {
      if (e.key === 'Escape' && this.modelValue) this.close()
    }
  }
}
</script>
