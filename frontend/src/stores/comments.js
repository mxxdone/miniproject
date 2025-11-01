import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '@/api'
import { useUiStore } from './ui'

export const useCommentsStore = defineStore('comments', () => {
  const uiStore = useUiStore()
  const comments = ref([])

  // 특정 게시물의 댓글 목록 불러오기
  async function fetchComments(postId) {
    comments.value = []
    try {
      const response = await apiClient.get(`api/v1/comments/post/${postId}`)
      comments.value = response.data
    } catch (error) {
      console.error('댓글을 불러오는 중 오류가 발생했습니다: ', error)
    }
  }

  // 새 댓글 작성
  async function createComment(payload) {
    try {
      await apiClient.post('/api/v1/comments', payload)
      // 작성 후 댓글 목록을 새로고침
      await fetchComments(payload.postId)
    } catch (error) {
      uiStore.showSnackbar({ text: '댓글 작성에 실패했습니다.', color: 'error' })
      console.error('댓글 작성 중 오류가 발생했습니다: ', error)
    }
  }

  async function deleteComment(postId, commentId, password) {
    try {
      // 게스트 댓글 삭제(비밀번호 필요)
      if (password) {
        await apiClient.delete(`/api/v1/comments/${commentId}/guest`, {
          data: { password: password } // 요청 본문에 비밀번호 추가
        })
      } else {
        // 회원/관리자 댓글 삭제
        await apiClient.delete(`/api/v1/comments/${commentId}`)
      }

      await fetchComments(postId)
      uiStore.showSnackbar({ text: '댓글이 삭제되었습니다.', color: 'success' })
    } catch (error) {
      if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        uiStore.showSnackbar({ text: '댓글 삭제 권한이 없습니다.', color: 'error' })
      } else {
        uiStore.showSnackbar({ text: '댓글 삭제 중 오류가 발생했습니다.', color: 'error' })
      }
    }
  }

  async function updateComment(postId, commentId, payload) {
    try {
      await apiClient.put(`/api/v1/comments/${commentId}`, payload)
      await fetchComments(postId)
      uiStore.showSnackbar({ text: '댓글이 수정되었습니다.', color: 'success' })
    } catch (error) {
      if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        uiStore.showSnackbar({ text: '댓글 수정 권한이 없습니다.', color: 'error' })
      } else {
        uiStore.showSnackbar({ text: '댓글 수정 중 오류가 발생했습니다.', color: 'error' })
      }
    }
  }

  return { comments, fetchComments, createComment, deleteComment, updateComment }
})
