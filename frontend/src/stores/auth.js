import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import apiClient from '@/api'
import router from '@/router'
import { jwtDecode } from 'jwt-decode'

export const useAuthStore = defineStore('auth', () => {
  // state
  const token = ref(localStorage.getItem('accessToken'))
  const refreshToken = ref(localStorage.getItem('refreshToken'))
  const username = ref(null)
  const userRole = ref(null)
  const isAdmin = computed(() => userRole.value === 'ROLE_ADMIN')
  const isLoggedIn = computed(() => !!token.value)

  // 시작 시 토큰 있으면 사용자 정보 초기화
  function initUserFromToken(tokenToDecode) {
    if (tokenToDecode) {
      try {
        const decoded = jwtDecode(tokenToDecode)
        username.value = decoded.sub
        userRole.value = decoded.auth
      } catch {
        // 토큰 파싱 실패 -> 모든 토큰 삭제
        setToken(null, null)
      }
    } else {
      username.value = null
      userRole.value = null
    }
  }

  // state 초기화: 앱 시작 시
  if (token.value) {
    initUserFromToken(token.value)
    refreshToken.value = localStorage.getItem('refreshToken')
  }

  // action 내 토큰 설정 로직
  function setToken(newAccessToken, newRefreshToken) {
    token.value = newAccessToken
    refreshToken.value = newRefreshToken
    if (newAccessToken) {
      localStorage.setItem('accessToken', newAccessToken)
    } else {
      localStorage.removeItem('accessToken')
    }

    if (newRefreshToken) {
      if (newRefreshToken) {
        localStorage.setItem('refreshToken', newRefreshToken)
      } else {
        localStorage.removeItem('refreshToken')
      }
    }
    initUserFromToken(newAccessToken)
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
      setToken(response.data.accessToken, response.data.refreshToken) // 로그인 성공시 토큰 저장

      const currentRoute = router.currentRoute.value
      const redirectPath = currentRoute.query.redirect || '/'
      await router.push(redirectPath)
    } catch (error) {
      alert('로그인에 실패했습니다: ' + (error.response?.data?.message || error.message))
    }
  }

  function logout() {
    setToken(null, null) // 토큰 삭제
    alert('로그아웃 되었습니다.')
    router.push('/')
  }

  return { token, refreshToken, username, userRole, isLoggedIn, isAdmin, setToken, signup, login, logout }
})
