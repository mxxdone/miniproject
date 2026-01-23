<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { useRoute, useRouter } from 'vue-router'

const authStore = useAuthStore()
const uiStore = useUiStore()
const router = useRouter()
const route = useRoute()

const form = ref(null)
const isFormValid = ref(false)
const isSubmitting = ref(false)

const isUsernameChecked = ref(false)
const isUsernameUnique = ref(false)
const isIdChecking = ref(false)

const isNicknameChecked = ref(false)
const isNicknameUnique = ref(false)
const isNicknameChecking = ref(false)

const usernameErrorMsg = ref('') // 에러 메시지 (빨간색)
const usernameSuccessMsg = ref('') // 성공 메시지 (초록색)
const nicknameErrorMsg = ref('')
const nicknameSuccessMsg = ref('')

const showPassword = ref(false)
const showConfirmPassword = ref(false)

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
})

const rules = {
  required: (value) => !!value || '필수 항목입니다.',
  minLength: (len) => (value) =>
    (value && value.length >= len) || `${len}자 이상으로 입력해주세요.`,
  maxLength: (len) => (value) => (value && value.length <= len) || `${len}자 이하로 입력해주세요.`,

  // 문자 종류만 검사하는 규칙
  username: (value) => {
    const pattern = /^[a-z0-9]+$/
    return pattern.test(value) || '영문 소문자와 숫자만 사용할 수 있습니다.'
  },
  password: (value) => {
    const pattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%^*#?&])[A-Za-z\d@$!%^*#?&]+$/
    return pattern.test(value) || '영문, 숫자, 특수문자를 포함해야 합니다.'
  },
  confirmPassword: (value) => {
    return value === formData.password || '비밀번호가 일치하지 않습니다.'
  },
  nickname: (value) => {
    const pattern = /^[a-zA-Z0-9가-힣]+$/
    return pattern.test(value) || '한글, 영문, 숫자만 사용할 수 있습니다.'
  },
  email: (value) => {
    const pattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/
    return pattern.test(value) || '유효한 이메일 주소를 입력해주세요.'
  },
}

const checkId = async () => {
  // 중복확인 버튼 누를 때마다 메시지 초기화
  usernameErrorMsg.value = ''
  usernameSuccessMsg.value = ''

  if (!formData.username) {
    usernameErrorMsg.value = '아이디를 입력해주세요.'
    return
  }

  const error = rules.username(formData.username)
  if (typeof error === 'string') {
    usernameErrorMsg.value = error
    return
  }

  isIdChecking.value = true
  try {
    const result = await authStore.checkDuplicateId(formData.username)
    isUsernameChecked.value = true // 버튼을 눌렀으므로 체크 완료 상태로 변경
    if (result.success) {
      if (result.isDuplicate) {
        isUsernameUnique.value = false
        usernameErrorMsg.value = '이미 사용 중인 아이디입니다.'
      } else {
        isUsernameUnique.value = true
        usernameSuccessMsg.value = '사용 가능한 아이디입니다.'
      }
    } else {
      isUsernameUnique.value = false
      usernameErrorMsg.value = result?.message || '아이디 중복 확인에 실패했습니다.'
    }
  } catch (error) {
    isUsernameUnique.value = false
    usernameErrorMsg.value = '네트워크 오류가 발생했습니다.'
  } finally {
    isIdChecking.value = false
  }
}

const checkNickname = async () => {
  nicknameErrorMsg.value = ''
  nicknameSuccessMsg.value = ''

  if (!formData.nickname) {
    nicknameErrorMsg.value = '닉네임을 입력해주세요.'
    return
  }

  const error = rules.nickname(formData.nickname)
  if (typeof error === 'string') {
    nicknameErrorMsg.value = error
    return
  }

  isNicknameChecking.value = true
  try {
    const result = await authStore.checkDuplicateNickname(formData.nickname)
    isNicknameChecked.value = true

    if (result.success) {
      if (result.isDuplicate) {
        isNicknameUnique.value = false
        nicknameErrorMsg.value = '이미 사용 중인 닉네임입니다.'
      } else {
        isNicknameUnique.value = true
        nicknameSuccessMsg.value = '사용 가능한 닉네임입니다.'
      }
    } else {
      isNicknameUnique.value = false
      nicknameErrorMsg.value = result?.message || '닉네임 중복 확인에 실패했습니다.'
    }
  } catch (error) {
    isNicknameUnique.value = false
    nicknameErrorMsg.value = '네트워크 오류가 발생했습니다.'
  } finally {
    isNicknameChecking.value = false
  }
}

// 값 수정 시 체크 상태 초기화
watch(
  () => formData.username,
  () => {
    isUsernameChecked.value = false
    isUsernameUnique.value = false
    usernameErrorMsg.value = ''
    usernameSuccessMsg.value = ''
  },
)
watch(
  () => formData.nickname,
  () => {
    isNicknameChecked.value = false
    isNicknameUnique.value = false
    nicknameErrorMsg.value = ''
    nicknameSuccessMsg.value = ''
  },
)

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
const googleLoginUrl = computed(() => {
  const redirect = encodeURIComponent(route.query.redirect || '/')
  return `${API_BASE_URL}/oauth2/authorization/google?redirect=${redirect}`
})

async function submitSignup() {
  // 아이디와 닉네임 중복 확인 여부 모두 체크
  if (!isUsernameChecked.value || !isUsernameUnique.value)
    return uiStore.showSnackbar({ text: '아이디 중복 확인이 필요합니다.', color: 'warning' })
  if (!isNicknameChecked.value || !isNicknameUnique.value)
    return uiStore.showSnackbar({ text: '닉네임 중복 확인이 필요합니다.', color: 'warning' })

  const { valid } = await form.value.validate()
  if (!valid) return

  isSubmitting.value = true
  try {
    const result = await authStore.signup({
      username: formData.username,
      password: formData.password,
      nickname: formData.nickname,
      email: formData.email,
    })

    if (result && (result.success || result.data)) {
      uiStore.showSnackbar({ text: '회원가입이 완료되었습니다!', color: 'success' })
      router.push('/login')
    } else {
      uiStore.showSnackbar({ text: result?.message || '가입에 실패했습니다.', color: 'error' })
    }
  } catch (error) {
    uiStore.showSnackbar({ text: '가입 처리 중 오류가 발생했습니다.', color: 'error' })
  } finally {
    isSubmitting.value = false
  }
}
</script>
<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card class="elevation-12 rounded-xl" border>
          <v-toolbar color="primary" flat>
            <v-toolbar-title class="text-white">회원가입</v-toolbar-title>
          </v-toolbar>

          <v-card-text class="pa-6">
            <v-form ref="form" v-model="isFormValid" lazy-validation @submit.prevent="submitSignup">
              <v-text-field
                v-model="formData.username"
                label="아이디"
                prepend-icon="mdi-account"
                :rules="[rules.required, rules.username, rules.minLength(4), rules.maxLength(12)]"
                variant="outlined"
                placeholder="4~12자 영문, 숫자"
                :error-messages="usernameErrorMsg"
                :messages="usernameSuccessMsg"
                persistent-hint
                class="mb-3"
              >
                <template #append-inner>
                  <v-btn
                    variant="text"
                    color="primary"
                    class="font-weight-bold"
                    @click="checkId"
                    :loading="isIdChecking"
                    >중복확인</v-btn
                  >
                </template>
              </v-text-field>

              <v-text-field
                v-model="formData.password"
                label="비밀번호"
                prepend-icon="mdi-lock"
                :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                :type="showPassword ? 'text' : 'password'"
                @click:append-inner="showPassword = !showPassword"
                :rules="[rules.required, rules.password, rules.minLength(8), rules.maxLength(20)]"
                variant="outlined"
                class="mb-2"
              ></v-text-field>

              <v-text-field
                v-model="formData.confirmPassword"
                label="비밀번호 확인"
                prepend-icon="mdi-lock-check"
                :append-inner-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
                :type="showConfirmPassword ? 'text' : 'password'"
                @click:append-inner="showConfirmPassword = !showConfirmPassword"
                :rules="[rules.required, rules.confirmPassword]"
                variant="outlined"
                class="mb-2"
              ></v-text-field>

              <v-text-field
                v-model="formData.nickname"
                label="닉네임"
                prepend-icon="mdi-badge-account-horizontal"
                :rules="[rules.required, rules.nickname, rules.minLength(2), rules.maxLength(10)]"
                variant="outlined"
                :error-messages="nicknameErrorMsg"
                :messages="nicknameSuccessMsg"
                persistent-hint
                class="mb-3"
              >
                <template #append-inner>
                  <v-btn
                    variant="text"
                    color="primary"
                    class="font-weight-bold"
                    @click="checkNickname"
                    :loading="isNicknameChecking"
                    >중복확인</v-btn
                  >
                </template>
              </v-text-field>

              <v-text-field
                v-model="formData.email"
                label="이메일"
                type="email"
                prepend-icon="mdi-email"
                :rules="[rules.required, rules.email]"
                variant="outlined"
              ></v-text-field>

              <v-btn
                type="submit"
                color="primary"
                block
                size="large"
                class="mt-6 font-weight-bold"
                :loading="isSubmitting"
                :disabled="!isFormValid"
              >
                가입하기
              </v-btn>
            </v-form>
            <a :href="googleLoginUrl" class="text-decoration-none">
              <v-btn
                color="red-lighten-1"
                block
                size="large"
                variant="elevated"
                class="text-none font-weight-bold"
              >
                <v-icon start icon="mdi-google"></v-icon>
                Google 계정으로 시작하기
              </v-btn>
            </a>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
