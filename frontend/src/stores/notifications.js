import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '@/api'

export const useNotificationsStore = defineStore('notifications', () => {
  const notifications = ref([])
  const unreadCount = ref(0)

/*  const unreadNotifications = computed(() => {
    // 안전장치: notifications.value가 없을 때를 대비해 빈 배열 처리
    const list = notifications.value || []
    return list.filter(n => !n.isRead)
  })*/

  // 안 읽은 알림 개수 가져오기 (배지용)
  async function fetchUnreadCount() {
    try {
      const response = await apiClient.get('/api/v1/notifications/unread-count')
      unreadCount.value = response.data
    } catch (error) {
      console.error('알림 개수 조회 실패:', error)
    }
  }

  // 알림 목록 가져오기 (목록용)
  async function fetchNotifications() {
    try {
      const response = await apiClient.get('/api/v1/notifications')
      notifications.value = response.data
    } catch (error) {
      console.error('알림 목록 조회 실패:', error)
    }
  }

  // 읽음 처리
  async function markAsRead(id) {
    try {
      await apiClient.patch(`/api/v1/notifications/${id}/read`)

      // 클릭한 알림 찾기
      const target = notifications.value.find((item) => item.id === id)
      if (target && !target.isRead) {
        target.isRead = true
        unreadCount.value = Math.max(0, unreadCount.value - 1) // 0이하로(음수가 나오는 것을 방지)
      }
    } catch (error) {
      console.error('알림 읽음 처리 실패:', error)
    }
  }

  // 전체 읽음 처리
  async function markAllAsRead() {
    try {
      await apiClient.patch('/api/v1/notifications/read-all')

      // 모든 알림을 '읽음'으로 변경
      notifications.value.forEach(n => n.isRead = true)
      // 안 읽은 개수는 0개로 초기화
      unreadCount.value = 0

    } catch (error) {
      console.error('전체 읽음 처리 실패:', error)
    }
  }

  return { notifications, unreadCount, fetchUnreadCount, fetchNotifications, markAsRead, markAllAsRead }
})
