<script setup>
import { useUiStore } from '@/stores/ui'
const uiStore = useUiStore()
</script>

<template>
  <v-app>
    <v-main>
      <RouterView />
    </v-main>

    <v-snackbar
      v-model="uiStore.snackbar.visible"
      :color="uiStore.snackbar.color"
      :timeout="uiStore.snackbar.timeout"
    >
      {{ uiStore.snackbar.text }}
    </v-snackbar>
  </v-app>
</template>
