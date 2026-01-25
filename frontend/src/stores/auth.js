import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import apiClient from '@/api'
import router from '@/router'
import {jwtDecode} from 'jwt-decode'

export const useAuthStore = defineStore('auth', () => {
  // state
  const token = ref(localStorage.getItem('accessToken'))
  const username = ref(null)
  const userRole = ref(null)
  const nickname = ref(null)
  const email = ref(null)
  const isSocialUser = ref(false)
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
          setToken(null) // 만료되었으면 토큰 제거
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
    initUserFromToken(token.value)
  }

  function clearAuth() {
    token.value = null
    username.value = null
    nickname.value = null
    userRole.value = null
    email.value = null
    isSocialUser.value = false
    localStorage.removeItem('accessToken')
    // axios 헤더에서도 삭제
    apiClient.defaults.headers.common['Authorization'] = null
  }

  async function signup(payload) {
    try {
      const res = await apiClient.post('/api/v1/users/signup', payload)
      return { success: true, data: res.data }
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || error.message,
      }
    }
  }

  async function login(payload) {
    try {
      const response = await apiClient.post('/api/v1/auth/login', payload)
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
      await apiClient.post('/api/v1/auth/logout')
    } catch (error) {
      console.error('백엔드 로그아웃 에러:', error)
    } finally {
      clearAuth()
    }
  }

  async function fetchProfile() {
    try {
      const response = await apiClient.get('/api/v1/users/info')
      const data = response.data // UserInfoResponseDto 구조

      // DB의 최신 정보로 스토어 상태 동기화
      username.value = data.username
      nickname.value = data.nickname
      userRole.value = data.role
      email.value = data.email
      isSocialUser.value = data.isSocialUser

      return { success: true, data }
    } catch (error) {
      console.error('프로필 조회 실패:', error)
      return {
        success: false,
        message: error.response?.data?.message || '정보를 불러오지 못했습니다.',
      }
    }
  }

  async function updateNickname(newNickname) {
    try {
      await apiClient.patch('/api/v1/users/nickname', { nickname: newNickname })
      this.nickname = newNickname // 스토어 상태 동기화
      return { success: true }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '닉네임 수정 실패' }
    }
  }

  async function updatePassword(currentPassword, newPassword) {
    try {
      await apiClient.patch('/api/v1/users/password', { currentPassword, newPassword })
      return { success: true }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || '비밀번호 수정 실패' }
    }
  }

  async function withdraw(password) {
    try {
      await apiClient.delete('/api/v1/users/info', { data: { password } })
      clearAuth()

      return { success: true }
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || '회원 탈퇴 처리 중 서버 오류가 발생했습니다.'
      }
    }
  }

  async function checkDuplicateId(username) {
    try {
      const response = await apiClient.get(`/api/v1/users/check-username`, {
        params: { username }
      });
      // response.data가 true면 중복, false면 사용 가능이라고 가정
      return { success: true, isDuplicate: response.data };
    } catch {
      return { success: false, message: '중복 확인 중 오류가 발생했습니다.' };
    }
  }

  async function checkDuplicateNickname(nickname) {
    try {
      const response = await apiClient.get('/api/v1/users/check-nickname', {
        params: { nickname },
      })
      return { success: true, isDuplicate: response.data }
    } catch {
      return { success: false, message: '닉네임 확인 중 에러 발생' }
    }
  }

  return {
    token,
    username,
    userRole,
    nickname,
    isLoggedIn,
    isAdmin,
    email,
    isSocialUser,
    setToken,
    signup,
    login,
    logout,
    fetchProfile,
    updateNickname,
    updatePassword,
    withdraw,
    checkDuplicateId,
    checkDuplicateNickname,
  }
})
