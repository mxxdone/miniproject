import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '@/api'

export const useCategoriesStore = defineStore('categories', () => {
  const categories = ref([])
  const isLoaded = ref(false)
  const isLoading = ref(false)

  async function fetchCategories(forceRefresh = false) {
    if (!forceRefresh && (isLoaded.value || isLoading.value)) {
      return // // 이미 카테고리 로드된 경우 API 호출 안 함
    }

    isLoading.value = true

    try {
      const response = await apiClient.get('/api/v1/categories')
      categories.value = response.data
      isLoaded.value = true
    } catch (error) {
      console.error('카테고리를 불러오는 중 오류가 발생했습니다: ', error)
    } finally {
      isLoading.value = false
    }
  }
  return {
    categories,
    isLoaded,
    isLoading,
    fetchCategories
  }})
