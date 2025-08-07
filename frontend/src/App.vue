<script setup>
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'

const uiStore = useUiStore()
const authStore = useAuthStore()
</script>

<template>
  <v-app>
    <!-- 상단 앱바 -->
    <v-app-bar app>
      <v-toolbar-title>
        <RouterLink to="/" class="text-decoration-none text-grey-darken-3">My Blog</RouterLink>
      </v-toolbar-title>
      <v-spacer></v-spacer>

      <!-- 로그인 상태에 따라 버튼 다르게 보여줌 -->
      <div v-if="!authStore.isLoggedIn">
        <v-btn to="/signup">회원가입</v-btn>
        <v-btn to="/login">로그인</v-btn>
      </div>
      <div v-else>
        <v-btn @click="authStore.logout()">로그아웃</v-btn>
      </div>
    </v-app-bar>

    <!-- 메인 뷰 -->
    <v-main>
      <RouterView />
    </v-main>

    <!-- 스낵바 (알림창) -->
    <v-snackbar
      v-model="uiStore.snackbar.visible"
      :color="uiStore.snackbar.color"
      :timeout="uiStore.snackbar.timeout"
    >
      {{ uiStore.snackbar.text }}
    </v-snackbar>
  </v-app>
</template>
