<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const username = ref('')
const password = ref('')

const submitLogin = () => {
  authStore.login({ username: username.value, password: password.value })
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
        <v-divider class="my-4"></v-divider>
        <a href="http://localhost:8080/oauth2/authorization/google" class="text-decoration-none">
          <v-btn color="red-lighten-1" block>
            <v-icon start>mdi-google</v-icon>
            Google로 로그인
          </v-btn>
        </a>
      </v-card-text>
    </v-card>
  </v-container>
</template>
