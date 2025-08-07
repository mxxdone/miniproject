<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const username = ref('')
const password = ref('')

const submitSignup = () => {
  authStore.signup({ username: username.value, password: password.value })
}
</script>

<template>
  <v-container>
    <v-card max-width="500" class="mx-auto">
      <v-card-title class="text-h5">회원가입</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="submitSignup">
          <v-text-field v-model="username" label="아이디" required></v-text-field>
          <v-text-field v-model="password" label="비밀번호" type="password" required></v-text-field>
          <v-btn type="submit" color="primary" block class="mt-4">가입하기</v-btn>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>
