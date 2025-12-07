<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const form = ref(null)
const username = ref('')
const password = ref('')
const nickname = ref('')
const email = ref('')

const rules = {
  required: value => !!value || '필수 항목입니다.',
  minLength: len => value => (value && value.length >= len) || `${len}자 이상으로 입력해주세요.`,
  maxLength: len => value => (value && value.length <= len) || `${len}자 이하로 입력해주세요.`,

  // 문자 종류만 검사하는 규칙
  username: value => {
    const pattern = /^[a-z0-9]+$/
    return pattern.test(value) || '영문 소문자와 숫자만 사용할 수 있습니다.'
  },
  password: value => {
    const pattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%^*#?&])[A-Za-z\d@$!%^*#?&]+$/
    return pattern.test(value) || '영문, 숫자, 특수문자를 포함해야 합니다.'
  },
  nickname: value => {
    const pattern = /^[a-zA-Z0-9가-힣]+$/;
    return pattern.test(value) || '한글, 영문, 숫자만 사용할 수 있습니다.'
  },
  email: value => {
    const pattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/
    return pattern.test(value) || '유효한 이메일 주소를 입력해주세요.'
  },
}

async function submitSignup() {
  const { valid } = await form.value.validate() // 폼 전체 유효성 검사
  if (valid) {
    authStore.signup({
      username: username.value,
      password: password.value,
      nickname: nickname.value,
      email: email.value,
    })
  }
}
</script>

<template>
  <v-container>
    <v-card max-width="500" class="mx-auto">
      <v-card-title class="text-h5">회원가입</v-card-title>
      <v-card-text>
        <v-form ref="form" @submit.prevent="submitSignup">
          <v-text-field
            v-model="username"
            label="아이디"
            :rules="[rules.required, rules.username, rules.minLength(4), rules.maxLength(12)]"
            required
          ></v-text-field>
          <v-text-field
            v-model="password"
            label="비밀번호"
            type="password"
            :rules="[rules.required, rules.password, rules.minLength(8), rules.maxLength(20)]"
            required
          ></v-text-field>
          <v-text-field
            v-model="nickname"
            label="닉네임"
            :rules="[rules.required, rules.nickname, rules.minLength(2), rules.maxLength(10)]"
            required
          ></v-text-field>
          <v-text-field
            v-model="email"
            label="이메일"
            type="email"
            :rules="[rules.required, rules.email]"
            required
          ></v-text-field>
          <v-btn type="submit" color="primary" block class="mt-4">가입하기</v-btn>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>
