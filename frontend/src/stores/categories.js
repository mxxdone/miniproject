import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '@/api'

export const useCategoriesStore = defineStore('categories', () => {
  const categories = ref({})

  async function fetchCategories() {
    try {
      const response = await apiClient.get('http://localhost:8080/api/v1/categories')
      categories.value = response.data
    } catch (error) {
      console.error('카테고리를 불러오는 중 오류가 발생했습니다: ', error)
    }
  }
  return { categories, fetchCategories }
})
