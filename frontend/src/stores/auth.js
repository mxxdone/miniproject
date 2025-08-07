import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import apiClient from '@/api'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  // state
  const token = ref(localStorage.getItem('jwt'))
  const isLoggedIn = computed(() => !!token.value)

  // action
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('jwt', newToken)
    } else {
      localStorage.removeItem('jwt')
    }
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
      setToken(response.data) // 로그인 성공시 토큰 저장
      await router.push('/')
    } catch (error) {
      alert('로그인에 실패했습니다: ' + (error.response?.data?.message || error.message))
    }
  }

  function logout() {
    setToken(null) // 토큰 삭제
    alert('로그아웃 되었습니다.')
    router.push('/')
  }

  return { token, isLoggedIn, signup, login, logout }
})
