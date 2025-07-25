import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

// 'posts'라는 이름의 스토어를 정의
export const usePostsStore = defineStore('posts', () => {
  // state
  const posts = ref([]) // 게시글 목록을 저장할 반응형 변수
  const currentPost = ref(null) // 단건 게시글을 저장할 상태 추가
  // 페이지 정보를 저장할 객체
  const page = ref({
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
    size: 3,
  })

  /** Actions **/

  // 게시글 생성
  async function createPost(newPost) {
    try {
      await axios.post('http://localhost:8080/api/v1/posts', newPost)
      return true
    } catch (error) {
      console.error('게시글 생성 중 오류가 발생했습니다:', error)
      return false
    }
  }

  // 게시글 목록 조회
  async function fetchPosts(pageNumber = 1) {
    try {
      // 백엔드 API 호출 (주소는 백엔드 서버 주소에 맞게 확인)
      const response = await axios.get(`http://localhost:8080/api/v1/posts?page=${pageNumber -1}&size=${page.value.size}`)
      // 현재 페이지의 상태 확정, 기록
      posts.value = response.data.content
      page.value.totalPages = response.data.totalPages
      page.value.totalElements = response.data.totalElements
      page.value.currentPage = response.data.number + 1
    } catch (error) {
      console.error('게시글을 불러오는 중 오류가 발생했습니다:', error)
    }
  }

  // ID로 게시글 단건 조회
  async function fetchPost(id) {
    try {
      const response = await axios.get(`http://localhost:8080/api/v1/posts/${id}`)
      currentPost.value = response.data
    } catch (error) {
      console.error(`${id}번 게시글을 불러오는 중 오류가 발생했습니다:`, error)
      currentPost.value = null
    }
  }

  // 게시글 삭제
  async function deletePost(id) {
    try {
      await axios.delete(`http://localhost:8080/api/v1/posts/${id}`)
      // 성공 시, 목록 페이지로 이동
      return true
    } catch (error) {
      console.error(`${id}번 게시글 삭제 중 오류가 발생했습니다:`, error)
      return false
    }
  }

  // 게시글 수정
  async function updatePost(id, postToUpdate){
    try {
      await axios.put(`http://localhost:8080/api/v1/posts/${id}`, postToUpdate)
      return true
    } catch (error) {
      console.error(`${id}번 게시글 수정 중 오류가 발생했습니다:`, error)
      return false
    }
  }

  // 외부 컴포넌트에서 사용할 수 있도록 정의
  return { posts, currentPost, page, fetchPosts, fetchPost, createPost, deletePost, updatePost}
})
