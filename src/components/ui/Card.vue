<template>
  <div :class="['card', compact ? 'p-3' : 'p-4']">
    <div v-if="title" class="flex items-center justify-between mb-2">
      <div>
        <div class="font-medium">{{ title }}</div>
        <div v-if="subTitle" class="text-sm text-gray-500">{{ subTitle }}</div>
      </div>
      <div>
        <slot name="actions" />
      </div>
    </div>
    <div>
      <slot />
    </div>
  </div>
</template>

<script>
export default {
  name: 'UiCard',
  props: {
    title: String,
    subTitle: String,
    compact: { type: Boolean, default: false }
  }
}
</script>
