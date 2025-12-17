<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTheme } from 'vuetify'
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import CategoryNav from '@/components/CategoryNav.vue'
import { jwtDecode } from 'jwt-decode'
import NotificationMenu from '@/components/NotificationMenu.vue'
import axios from 'axios'

const uiStore = useUiStore()
const authStore = useAuthStore()
const drawer = ref(true) // 카테고리 서랍 열림 닫힘 제어
const theme = useTheme()
const isDark = computed(() => theme.global.current.value.dark)

function toggleTheme() {
  // 현재 테마가 다크 모드인지 확인하고, 아니면 anyangDark로, 맞으면 anyangLight
  const newTheme = isDark.value ? 'anyangLight' : 'anyangDark'
  // 현재 테마 변경
  theme.change(newTheme)
  // 선택한 테마를 localStorage에 저장
  localStorage.setItem('theme', newTheme)
}

onMounted(async () => {
  // 토큰 유효성 검사
  const token = localStorage.getItem('accessToken') // localStorage에서 직접 확인
  if (token) {
    try {
      const decoded = jwtDecode(token)
      const currentTime = Date.now() / 1000 // 현재 시간 (초 단위)

      // 토큰 만료 시간이 현재 시간보다 이전이면 만료된 것
      if (decoded.exp < currentTime) {
        console.warn('로드 시 액세스 토큰이 만료되었습니다. 토큰 재발급을 시도합니다.')
        try {
          const response = await axios.post(
            `${import.meta.env.VITE_API_BASE_URL}/api/v1/auth/refresh`,
            {},
            { withCredentials: true }, // 쿠키 전송 필수
          )
          // 재발급 성공 시: 새로운 토큰으로 스토어 업데이트 (로그인 유지)
          const { accessToken } = response.data
          authStore.setToken(accessToken)
          console.log('토큰 재발급 성공, 로그인 상태를 유지합니다.')
        } catch (refreshError) {
          // 재발급 실패 시(리프레시 토큰도 만료 등): 로그아웃 처리
          console.error('토큰 재발급 실패, 로그아웃 합니다:', refreshError)
          authStore.logout()
        }
      } else {
        // 만료되지 않았다면 스토어 상태 초기화
        authStore.setToken(token)
      }
    } catch (e) {
      console.error('유효하지 않은 토큰입니다:', e)
      authStore.logout() // 토큰 해석 실패 시 로그아웃
    }
  }

  // 페이지가 로드될 때 테마 값 설정
  const savedTheme = localStorage.getItem('theme')

  if (savedTheme) {
    theme.change(savedTheme)
  }
})
</script>

<template>
  <v-app>
    <v-layout>
      <!-- 보라색 상단바 + 내부 텍스트/아이콘 on-primary로 통일 -->
      <v-app-bar color="primary" flat>
        <v-app-bar-nav-icon variant="text" color="on-primary" @click="drawer = !drawer" />

        <v-toolbar-title class="text-white">
          <RouterLink to="/" class="text-decoration-none text-white"> MOODONE.DEV </RouterLink>
        </v-toolbar-title>

        <v-spacer />

        <NotificationMenu v-if="authStore.isLoggedIn" />

        <!-- 토글: 아이콘 버튼 권장 -->
        <v-btn variant="text" icon color="on-primary" @click="toggleTheme" class="mr-2">
          <v-icon>{{ isDark ? 'mdi-weather-sunny' : 'mdi-weather-night' }}</v-icon>
        </v-btn>

        <!-- 인증 버튼들 -->
        <template v-if="!authStore.isLoggedIn">
          <v-btn variant="text" color="on-primary" to="/signup">회원가입</v-btn>
          <v-btn variant="text" color="on-primary" to="/login">로그인</v-btn>
        </template>
        <template v-else>
          <v-chip color="on-primary" variant="text" class="mr-2">
            {{ authStore.nickname }} 님
          </v-chip>
          <v-btn variant="text" color="on-primary" @click="authStore.logout()">로그아웃</v-btn>
        </template>
      </v-app-bar>

      <!-- Drawer: 컴포넌트 이름 수정 -->
      <v-navigation-drawer v-model="drawer">
        <CategoryNav />
        <template v-slot:append>
          <div class="pa-2 text-caption text-center text-medium-emphasis">
            © 2025. mxxdone. All rights reserved.
          </div>
        </template>
      </v-navigation-drawer>

      <v-main>
        <RouterView />
      </v-main>
    </v-layout>

    <v-snackbar
      v-model="uiStore.snackbar.visible"
      :color="uiStore.snackbar.color"
      :timeout="uiStore.snackbar.timeout"
    >
      {{ uiStore.snackbar.text }}
    </v-snackbar>
  </v-app>
</template>
