<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const loading = ref(true)

// URL 쿼리 파라미터가 배열일 경우 대비 첫 값만 가져오기
function getFirstQueryParam(param) {
  const value = route.query[param]
  return Array.isArray(value) ? value[0] : value
}

onMounted(async () => {
  try {
    const token = getFirstQueryParam('token')
    const redirectPath = getFirstQueryParam('redirect')

    if (!token) {
      throw new Error('인증 토큰이 없습니다.')
    }

    authStore.setToken(token)

    //redirectPath가 있으면 해당 경로로, 없으면 메인('/')으로 이동
    await router.replace(redirectPath || '/')
  } catch {
    alert('로그인 처리 중 문제가 발생하였습니다. 다시 시도해 주세요.')
    await router.replace('/login')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="auto">
        <v-progress-circular
          v-if="loading"
          indeterminate
          color="primary"
          size="48"
        ></v-progress-circular>
        <div v-else>로그인 완료!</div>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped>
.fill-height {
  height: 80vh;
}
</style>
