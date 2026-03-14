// src/stores/admin.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '@/api'

export const useAdminStore = defineStore('admin', () => {
  const summary = ref(null)
  const popularPosts = ref([])
  const recentComments = ref([])
  const commentsDailyStats = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function fetchDashboard() {
    loading.value = true
    error.value = null
    try {
      const [summaryRes, popularRes, commentsRes, dailyRes] = await Promise.all([
        apiClient.get('/api/v1/admin/stats/summary'),
        apiClient.get('/api/v1/admin/stats/popular-posts', { params: { limit: 5 } }),
        apiClient.get('/api/v1/admin/stats/recent-comments', { params: { limit: 10 } }),
        apiClient.get('/api/v1/admin/stats/comments-daily', { params: { days: 30 } }),
      ])
      summary.value = summaryRes.data
      popularPosts.value = popularRes.data
      recentComments.value = commentsRes.data
      commentsDailyStats.value = dailyRes.data
    } catch (e) {
      error.value = e.response?.data?.message || '통계 데이터를 불러오지 못했습니다.'
    } finally {
      loading.value = false
    }
  }

  return {
    summary,
    popularPosts,
    recentComments,
    commentsDailyStats,
    loading,
    error,
    fetchDashboard,
  }
})
