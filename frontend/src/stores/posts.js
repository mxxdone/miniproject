import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '@/api'
import { useUiStore } from './ui'

// 'posts'라는 이름의 스토어를 정의
export const usePostsStore = defineStore('posts', () => {
  // state
  const posts = ref([]) // 게시글 목록을 저장할 반응형 변수
  const currentPost = ref(null) // 단건 게시글을 저장할 상태 추가
  // 페이지 정보를 저장할 객체
  const page = ref({
    totalPages: 0,
    totalElements: 0,
    currentPage: 1,
    size: 6,
  })
  const currentCategoryId = ref(null) // 현재 선택된 카테고리id를 저장할 상태 추가
  const currentSearch = ref({ type: 'all', keyword: '' }) // 기본은 전체 검색
  const uiStore = useUiStore()

  /** Actions **/

  // 게시글 생성
  async function createPost(newPost) {
    try {
      await apiClient.post('http://localhost:8080/api/v1/posts', newPost)
      return true
    } catch (error) {
      console.error('게시글 생성 중 오류가 발생했습니다:', error)
      return false
    }
  }

  // 게시글 목록 조회
  async function fetchPosts({ pageNumber = 1, categoryId = null, type = 'all', keyword = '' }) {
    posts.value = [] // 게시글 불러오기 전, 기존 목록 비우기

    currentCategoryId.value = categoryId
    currentSearch.value = { type, keyword }

    try {
      const response = await apiClient.get('/api/v1/posts', {
        params: {
          page: pageNumber - 1,
          size: page.value.size,
          categoryId: categoryId,
          type: type,
          keyword: keyword
        }
      })

      const data = response.data
      posts.value = data.content
      page.value = {
        ...page.value,
        totalPages:  data.totalPages,
        totalElements: data.totalElements,
        currentPage: data.pageNumber + 1
      }
    } catch (error) {
      console.error('게시물을 불러오는 중 오류가 발생했습니다: ', error)
    }
  }

  // ID로 게시글 단건 조회
  async function fetchPost(id) {
    currentPost.value = null
    try {
      const response = await apiClient.get(`http://localhost:8080/api/v1/posts/${id}`)
      currentPost.value = response.data
    } catch (error) {
      console.error(`${id}번 게시글을 불러오는 중 오류가 발생했습니다:`, error)
      currentPost.value = null
    }
  }

  // 게시글 삭제
  async function deletePost(id) {
    try {
      await apiClient.delete(`http://localhost:8080/api/v1/posts/${id}`)
      // 성공 시, 목록 페이지로 이동
      return true
    } catch (error) {
      if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        uiStore.showSnackbar({ text: '이 게시글을 삭제할 권한이 없습니다.', color: 'error' })
      } else {
        uiStore.showSnackbar({ text: '게시글 삭제 중 오류가 발생했습니다', color: 'error' })
      }
      console.error(`${id}번 게시글 삭제 중 오류가 발생했습니다:`, error);
      return false
    }
  }

  // 게시글 수정
  async function updatePost(id, postToUpdate){
    try {
      await apiClient.put(`http://localhost:8080/api/v1/posts/${id}`, postToUpdate)
      return true
    } catch (error) {
      if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        uiStore.showSnackbar({ text: '이 게시글을 수정할 권한이 없습니다.', color: 'error' });
      } else {
        uiStore.showSnackbar({ text: '게시글 수정 중 오류가 발생했습니다.', color: 'error' });
      }
      console.error(`${id}번 게시글 수정 중 오류가 발생했습니다:`, error)
      return false
    }
  }

  // 외부 컴포넌트에서 사용할 수 있도록 정의
  return { posts, currentPost, page, currentCategoryId, fetchPosts, fetchPost, createPost, deletePost, updatePost}
})
