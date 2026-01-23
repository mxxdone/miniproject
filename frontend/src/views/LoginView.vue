<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui.js'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUiStore()
const username = ref('')
const password = ref('')

// 환경 변수에서 백엔드 베이스 URL 가져오기
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

// 구글 로그인 URL 생성
// 현재 페이지의 redirect 쿼리가 바뀔 때마다 URL도 자동으로 업데이트
const googleLoginUrl = computed(() => {
  const redirect = encodeURIComponent(route.query.redirect || '/')
  // 백엔드의 구글 로그인 엔드포인트에 redirect 파라미터를 추가하여 전달
  return `${API_BASE_URL}/oauth2/authorization/google?redirect=${redirect}`
})

const submitLogin = async () => {
  const isSuccess = await authStore.login({
    username: username.value,
    password: password.value,
  })

  if (isSuccess) {
    uiStore.showSnackbar({ text: '로그인되었습니다.', color: 'success' })

    // 로그인이 성공하면 쿼리에 담긴 redirect 경로로 이동
    const redirectPath = route.query.redirect || '/'
    await router.push(redirectPath)
  }
}
</script>

<template>
  <v-container>
    <v-card max-width="500" class="mx-auto">
      <v-card-title class="text-h5">로그인</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="submitLogin">
          <v-text-field v-model="username" label="아이디" required></v-text-field>
          <v-text-field v-model="password" label="비밀번호" type="password" required></v-text-field>
          <v-btn type="submit" color="primary" block class="mt-4">로그인</v-btn>
        </v-form>
        <a :href="googleLoginUrl" class="text-decoration-none">
          <v-btn
            color="red-lighten-1"
            block
            size="large"
            variant="elevated"
            class="text-none font-weight-bold"
          >
            <v-icon start>mdi-google</v-icon>
            Google 계정으로 시작하기
          </v-btn>
        </a>
      </v-card-text>
    </v-card>
  </v-container>
</template>
