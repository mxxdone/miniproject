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
    currentCategoryId.value = categoryId
    currentSearch.value = { type, keyword }

    let url = 'http://localhost:8080/api/v1/posts'
    let params = {
      page: pageNumber - 1,
      size: page.value.size
    }

    if (keyword) { // 키워드가 있으면 검색 API 사용
      url = `http://localhost:8080/api/v1/posts/search`
      params.keyword = keyword
      params.type = type
      if (categoryId) {
        params.categoryId = categoryId;
      }
    } else if (categoryId) { // 카테고리 IPD가 있으면 카테고리별 조회 API 사용
      url = `http://localhost:8080/api/v1/posts/category/${categoryId}`
    }

    try {
      // 백엔드 API 호출 (주소는 백엔드 서버 주소에 맞게 확인)
      const response = await apiClient.get(url, { params })
      // 현재 페이지의 상태 확정, 기록
      const data = response.data
      posts.value = data.content
      page.value = {
        //기존 page 상태 객체의 내용을 복사해서 아래 내용들을 덮어쓰기
        // a.g. Size값 고정
        ...page.value,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
        currentPage: data.number + 1, // 프론트용 1-based
      }

    } catch (error) {
      console.error('게시글을 불러오는 중 오류가 발생했습니다:', error)
    }
  }

  // ID로 게시글 단건 조회
  async function fetchPost(id) {
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
