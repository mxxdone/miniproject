import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const snackbar = ref({
    visible: false,
    text: '',
    color: 'info',
    timeout: 3000,
  })

  function showSnackbar({ text, color = 'info', timeout = 3000 }) {
    snackbar.value = {
      visible: true,
      text,
      color,
      timeout,
    }
  }

  return { snackbar, showSnackbar }
})
