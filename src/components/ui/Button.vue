<template>
  <button :class="classes" :disabled="disabled" @click="$emit('click')">
    <slot />
  </button>
</template>

<script>
export default {
  name: 'UiButton',
  props: {
    variant: { type: String, default: 'primary' },
    disabled: { type: Boolean, default: false }
  },
  computed: {
    classes() {
      const base = 'inline-flex items-center px-3 py-1.5 rounded-md text-sm'
      if (this.variant === 'primary') return base + ' bg-primary text-white'
      if (this.variant === 'secondary') return base + ' border border-gray-200 text-gray-700 bg-white'
      return base + ' bg-transparent'
    }
  }
}
</script>
