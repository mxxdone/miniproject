import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import apiClient from '@/api'
import router from '@/router'
import { jwtDecode } from 'jwt-decode'

export const useAuthStore = defineStore('auth', () => {
  // state
  const token = ref(localStorage.getItem('accessToken'))
  const username = ref(null)
  const userRole = ref(null)
  const nickname = ref(null)
  const isAdmin = computed(() => userRole.value === 'ROLE_ADMIN')
  const isLoggedIn = computed(() => !!token.value)

  // 시작 시 토큰 있으면 사용자 정보 초기화
  function initUserFromToken(tokenToDecode) {
    if (tokenToDecode) {
      try {
        const decoded = jwtDecode(tokenToDecode)
        // 액세스 토큰 유효기간 추가 검사
        const currentTime = Date.now() / 1000; // 밀리초를 초로 변환
        if (decoded.exp < currentTime) {
          console.warn('초기화 과정에서 액세스 토큰이 이미 만료되었습니다.');
          setToken(null); // 만료되었으면 토큰 제거
        } else {
          username.value = decoded.sub
          userRole.value = decoded.auth
          nickname.value = decoded.nickname
        }
      } catch {
        // 토큰 파싱 실패 -> 토큰 삭제
        setToken(null)
      }
    } else {
      username.value = null
      userRole.value = null
      nickname.value = null
    }
  }

  // action 내 토큰 설정 로직
  function setToken(newAccessToken) {
    token.value = newAccessToken
    if (newAccessToken) {
      localStorage.setItem('accessToken', newAccessToken)
    } else {
      localStorage.removeItem('accessToken')
    }

    initUserFromToken(newAccessToken)
  }

  // 앱 시작 시 초기화 로직
  if (token.value) {
    initUserFromToken(token.value);
  }

  async function signup(payload) {
    try {
      await apiClient.post('/api/v1/users/signup', payload)
      alert('회원가입에 성공했습니다. 로그인해주세요.')
      await router.push('/login')
    } catch (error) {
      alert('회원가입에 실패했습니다: ' + (error.response?.data?.message || error.message))
    }
  }

  async function login(payload) {
    try {
      const response = await apiClient.post('/api/v1/users/login', payload)
      setToken(response.data.accessToken) // 로그인 성공시 토큰 저장

      const currentRoute = router.currentRoute.value
      const redirectPath = currentRoute.query.redirect || '/'
      await router.push(redirectPath)
    } catch (error) {
      alert('로그인에 실패했습니다: ' + (error.response?.data?.message || error.message))
    }
  }

  async function logout() {
    try {
      await apiClient.post('/api/v1/auth/logout');
    } catch (error) {
      console.error('백엔드 로그아웃 에러:', error);
    } finally {
      // API 호출 성공 여부와 관계없이 프론트엔드 상태 초기화 진행
      setToken(null); // 로컬 스토리지 액세스 토큰 및 상태 초기화
    }
  }

  return { token, username, userRole, nickname, isLoggedIn, isAdmin, setToken, signup, login, logout }
})
