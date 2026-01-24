<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDisplay, useTheme } from 'vuetify'
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import CategoryNav from '@/components/CategoryNav.vue'
import { jwtDecode } from 'jwt-decode'
import NotificationMenu from '@/components/NotificationMenu.vue'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const uiStore = useUiStore()
const authStore = useAuthStore()
const drawer = ref(true) // 카테고리 서랍 열림 닫힘 제어
const theme = useTheme()
const isDark = computed(() => theme.global.current.value.dark)
const { mobile } = useDisplay()

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
          await authStore.logout()
        }
      } else {
        // 만료되지 않았다면 스토어 상태 초기화
        authStore.setToken(token)
      }
    } catch (e) {
      console.error('유효하지 않은 토큰입니다:', e)
      await authStore.logout() // 토큰 해석 실패 시 로그아웃
    }
  }

  // 페이지가 로드될 때 테마 값 설정
  const savedTheme = localStorage.getItem('theme')

  if (savedTheme) {
    theme.change(savedTheme)
  }
})

const handleLogout = async () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    // 로그아웃 버튼을 누른 시점의 라우트 정보를 미리 변수에 고정
    const currentRouteName = route.name
    // 안전하게 현재 상태를 캡처
    const currentRouteParams = { ...route.params }

    await authStore.logout()
    uiStore.showSnackbar({ text: '로그아웃되었습니다.', color: 'success' })

    // 상황별 리다이렉트 로직 적용
    if (currentRouteName === 'postEdit') {
      // 수정 중 로그아웃 시: 원본 게시글 상세 페이지로 이동
      await router.push({
        name: 'postDetail',
        params: {
          parentSlug: currentRouteParams.parentSlug,
          childSlug: currentRouteParams.childSlug,
          id: currentRouteParams.id,
        },
      })
    } else if (currentRouteName === 'postCreate') {
      // 작성 중 로그아웃 -> 해당 카테고리 게시글 목록으로 이동
      if (currentRouteParams.parentSlug) {
        const targetPath = currentRouteParams.childSlug
          ? `/${currentRouteParams.parentSlug}/${currentRouteParams.childSlug}`
          : `/${currentRouteParams.parentSlug}`
        await router.push(targetPath)
      } else {
        await router.push('/') // 정보가 없다면 홈으로
      }
    }
    // 그 외 일반 페이지(목록, 상세)에서는 페이지 이동 없기 그대로 머문다.
  }
}
</script>

<template>
  <v-app>
    <v-layout>
      <!-- 보라색 상단바 + 내부 텍스트/아이콘 on-primary로 통일 -->
      <v-app-bar color="primary" flat>
        <v-app-bar-nav-icon variant="text" color="on-primary" @click="drawer = !drawer" />

        <v-toolbar-title class="text-white flex-grow-1 text-subtitle-1 text-sm-h6 font-weight-bold ml-n2 ml-sm-0">
          <RouterLink
            to="/"
            class="text-decoration-none text-white d-flex align-center h-100"
          >
            MOODONE.DEV
          </RouterLink>
        </v-toolbar-title>

        <div class="d-flex align-center ga-0 ga-sm-2">
          <NotificationMenu v-if="authStore.isLoggedIn" />

          <v-btn
            variant="text"
            color="on-primary"
            @click="toggleTheme"
            min-width="0"
            density="comfortable"
          >
            <v-icon>{{ isDark ? 'mdi-weather-sunny' : 'mdi-weather-night' }}</v-icon>
          </v-btn>

          <template v-if="!authStore.isLoggedIn">
            <v-btn
              variant="text"
              color="on-primary"
              to="/signup"
              min-width="0"
              density="comfortable"
            >
              <v-icon class="d-sm-none">mdi-account-plus</v-icon>
              <span class="d-none d-sm-block">회원가입</span>
            </v-btn>
            <v-btn
              variant="text"
              color="on-primary"
              :to="{ path: '/login', query: { redirect: $route.fullPath } }"
              min-width="0"
              density="comfortable"
            >
              <v-icon class="d-sm-none">mdi-login</v-icon>
              <span class="d-none d-sm-block">로그인</span>
            </v-btn>
          </template>
          <template v-else>
            <v-chip color="on-primary" variant="text" class="d-none d-sm-flex">
              {{ authStore.nickname }} 님
            </v-chip>

            <v-btn
              variant="text"
              color="on-primary"
              to="/mypage"
              min-width="0"
              density="comfortable"
            >
              <v-icon class="d-sm-none">mdi-account</v-icon>
              <span class="d-none d-sm-block">마이페이지</span>
            </v-btn>

            <v-btn
              variant="text"
              color="on-primary"
              @click="handleLogout"
              min-width="0"
              density="comfortable"
            >
              <v-icon class="d-sm-none">mdi-logout</v-icon>
              <span class="d-none d-sm-block">로그아웃</span>
            </v-btn>
          </template>
        </div>
      </v-app-bar>

      <!-- Drawer: 컴포넌트 이름 수정 -->
      <v-navigation-drawer
        v-model="drawer"
        :width="mobile ? undefined : 300"
        :temporary="mobile"
        :permanent="!mobile"
      >
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
