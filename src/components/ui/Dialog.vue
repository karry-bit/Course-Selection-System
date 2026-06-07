<template>
  <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="absolute inset-0 bg-black/40" @click="close"></div>
    <div class="bg-white rounded-lg p-6 z-10 w-11/12 max-w-md">
      <div class="flex justify-between items-center mb-3">
        <div class="font-medium text-lg">{{ title }}</div>
        <button class="text-gray-500" @click="close">✕</button>
      </div>
      <div>
        <slot />
      </div>
      <div class="mt-4 flex justify-end gap-2">
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
  methods: {
    close() { this.$emit('update:modelValue', false) }
  }
}
</script>
