<script setup>
import { ref, computed, onMounted } from 'vue'
import { useTheme } from 'vuetify'
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import CategoryNav from '@/components/CategoryNav.vue'

const uiStore = useUiStore()
const authStore = useAuthStore()
const drawer = ref(true) // 카테고리 서랍 열림 닫힘 제어
const theme = useTheme()
const isDark = computed(() => theme.global.current.value.dark)

function toggleTheme() {
  // 현재 테마가 다크 모드인지 확인하고, 아니면 anyangDark로, 맞으면 anyangLight
  const newTheme = theme.global.name.value = isDark.value ? 'anyangLight' : 'anyangDark'
  // 현재 테마 변경
  theme.global.name.value = newTheme
  // 선택한 테마를 localStorage에 저장
  localStorage.setItem('theme', newTheme)
}

// 페이지가 로드될 때 테마 값 설정
onMounted(() => {
  const savedTheme = localStorage.getItem('theme')

  if (savedTheme) {
    theme.global.name.value = savedTheme
  }
})
</script>

<template>
  <v-app>
    <v-layout>
      <!-- 보라색 상단바 + 내부 텍스트/아이콘 on-primary로 통일 -->
      <v-app-bar color="primary" flat>
        <v-app-bar-nav-icon
          variant="text"
          color="on-primary"
          @click="drawer = !drawer"
        />

        <v-toolbar-title class="text-white">
          <RouterLink to="/" class="text-decoration-none text-white">
            My Blog
          </RouterLink>
        </v-toolbar-title>

        <v-spacer />

        <!-- 토글: 아이콘 버튼 권장 -->
        <v-btn variant="text" icon color="on-primary" @click="toggleTheme">
          <v-icon>{{ isDark ? 'mdi-weather-sunny' : 'mdi-weather-night' }}</v-icon>
        </v-btn>

        <!-- 인증 버튼들 -->
        <template v-if="!authStore.isLoggedIn">
          <v-btn variant="text" color="on-primary" to="/signup">회원가입</v-btn>
          <v-btn variant="text" color="on-primary" to="/login">로그인</v-btn>
        </template>
        <template v-else>
          <v-btn variant="text" color="on-primary" @click="authStore.logout()">로그아웃</v-btn>
        </template>
      </v-app-bar>

      <!-- Drawer: 컴포넌트 이름 수정 -->
      <v-navigation-drawer v-model="drawer">
        <CategoryNav />
        <!-- 또는 <category-nav /> -->
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
