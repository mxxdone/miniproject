<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useUiStore } from '@/stores/ui'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const uiStore = useUiStore()
const router = useRouter()

const isLoading = ref(true)
const myPageForm = ref(null)
const isFormValid = ref(false)
const isSubmitting = ref(false)
const showCurrent = ref(false)
const showNew = ref(false)

// 초기값 설정: 닉네임은 스토어에서 가져옴
const formData = reactive({
  nickname: authStore.nickname || '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const rules = {
  required: [(v) => !!v || '필수 입력 항목입니다.'],
  nickname: [
    (v) => !!v || '닉네임을 입력해주세요.',
    (v) => (v && v.length >= 2 && v.length <= 10) || '닉네임은 2~10자여야 합니다.',
  ],
  password: [
    (v) => !!v || '새 비밀번호를 입력해주세요.',
    (v) =>
      /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%^*#?&])[A-Za-z\d@$!%^*#?&]{8,20}$/.test(v) ||
      '영문, 숫자, 특수문자(@$!%^*#?&)를 포함한 8~20자여야 합니다.',
  ],
  confirmPassword: [
    (v) => !!v || '비밀번호 확인을 입력해주세요.',
    (v) => v === formData.newPassword || '새 비밀번호와 일치하지 않습니다.',
  ],
}

const handleSave = async () => {
  const { valid } = await myPageForm.value.validate()
  if (!valid) return

  isSubmitting.value = true

  try {
    // [1] 닉네임 변경 체크
    // 입력한 닉네임이 기존 스토어에 저장된 닉네임과 다를 때만 호출
    if (formData.nickname !== authStore.nickname) {
      const res = await authStore.updateNickname(formData.nickname)
      if (!res.success) throw new Error(res.message)
    }

    // [2] 비밀번호 변경 체크
    // 새 비밀번호 필드에 값이 있을 때만 호출 (소셜 유저는 이 필드가 없어서 자동 패스)
    if (formData.newPassword) {
      const res = await authStore.updatePassword(formData.currentPassword, formData.newPassword)
      if (!res.success) throw new Error(res.message)

      // 비밀번호 수정 성공 시 입력 필드 비워주기
      formData.currentPassword = ''
      formData.newPassword = ''
      formData.confirmPassword = ''
    }

    uiStore.showSnackbar({ text: '회원 정보가 성공적으로 수정되었습니다.', color: 'success' })
    setTimeout(() => {
      router.push('/')
    }, 1200)
  } catch (error) {
    uiStore.showSnackbar({ text: error.message, color: 'error' })
  } finally {
    isSubmitting.value = false
  }
}

const handleWithdraw = async () => {
  // 소셜 유저가 아닌 경우 비밀번호 입력 여부 체크
  if (!authStore.isSocialUser && !formData.currentPassword) {
    uiStore.showSnackbar({
      text: '탈퇴 확인을 위해 현재 비밀번호를 입력해주세요.',
      color: 'warning',
    })
    return
  }

  // 최종 확인 대화상자
  if (!confirm('정말로 탈퇴하시겠습니까? 탈퇴 처리된 계정은 복구할 수 없습니다.')) {
    return
  }

  isSubmitting.value = true
  try {
    // 3. authStore의 withdraw 액션 호출 (위에서 만든 DELETE /api/v1/users/info)
    const result = await authStore.withdraw(formData.currentPassword)

    if (result.success) {
      uiStore.showSnackbar({ text: '회원 탈퇴가 완료되었습니다.', color: 'success' })
      router.push('/') // 탈퇴 후 홈으로 이동
    } else {
      uiStore.showSnackbar({ text: result.message, color: 'error' })
    }
  } catch (error) {
    console.log(error);
    uiStore.showSnackbar({ text: '통신 중 오류가 발생했습니다.', color: 'error' })
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  isLoading.value = true // 데이터 로드 시작
  try {
    const result = await authStore.fetchProfile()
    if (result.success) {
      formData.nickname = result.data.nickname
    } else {
      router.push('/login')
    }
  } finally {
    isLoading.value = false // 로드 완료 (성공/실패 상관없이)
  }
})
</script>

<template>
  <v-container v-if="isLoading" class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-skeleton-loader
          elevation="12 mt-n12"
          type="heading, text, list-item, text, list-item, list-item, actions"
          class="pa-6 rounded-xl"
        ></v-skeleton-loader>
      </v-col>
    </v-row>
  </v-container>
  <v-container v-else class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card class="elevation-12 mt-n12" border>
          <v-toolbar color="primary" flat>
            <v-toolbar-title class="text-white">마이페이지 / 정보 수정</v-toolbar-title>
          </v-toolbar>

          <v-card-text class="pa-6">
            <v-form ref="myPageForm" v-model="isFormValid" lazy-validation>
              <v-text-field
                :model-value="authStore.username"
                label="아이디"
                prepend-icon="mdi-account-circle"
                variant="filled"
                disabled
                class="mb-2"
              ></v-text-field>

              <v-text-field
                v-model="formData.nickname"
                label="닉네임"
                prepend-icon="mdi-badge-account-horizontal"
                :rules="rules.nickname"
                variant="outlined"
                clearable
                class="mb-2"
              ></v-text-field>

              <template v-if="!authStore.isSocialUser">
                <v-divider class="my-4"></v-divider>
                <p class="text-caption text-grey-darken-1 mb-4">
                  * 정보를 수정하려면 현재 비밀번호를 입력하고 새로운 비밀번호를 설정해주세요.
                </p>

                <v-text-field
                  v-model="formData.currentPassword"
                  label="현재 비밀번호 확인"
                  prepend-icon="mdi-lock-check"
                  :append-inner-icon="showCurrent ? 'mdi-eye' : 'mdi-eye-off'"
                  :type="showCurrent ? 'text' : 'password'"
                  @click:append-inner="showCurrent = !showCurrent"
                  :rules="rules.required"
                  variant="outlined"
                  class="mb-2"
                ></v-text-field>

                <v-text-field
                  v-model="formData.newPassword"
                  label="새 비밀번호 설정"
                  prepend-icon="mdi-lock-reset"
                  :append-inner-icon="showNew ? 'mdi-eye' : 'mdi-eye-off'"
                  :type="showNew ? 'text' : 'password'"
                  @click:append-inner="showNew = !showNew"
                  :rules="formData.newPassword ? rules.password : []"
                  variant="outlined"
                  hint="영문, 숫자, 특수문자 포함 8~20자"
                  persistent-hint
                  class="mb-4"
                ></v-text-field>

                <v-text-field
                  v-model="formData.confirmPassword"
                  label="새 비밀번호 확인"
                  prepend-icon="mdi-lock-check"
                  type="password"
                  :rules="formData.newPassword ? rules.confirmPassword : []"
                  variant="outlined"
                ></v-text-field>
              </template>
              <v-alert v-else type="info" variant="tonal" class="mt-4" icon="mdi-information">
                소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.
              </v-alert>
            </v-form>
          </v-card-text>

          <v-card-actions class="pa-6">
            <v-btn variant="text" color="error" class="font-weight-bold" @click="handleWithdraw">
              회원 탈퇴
            </v-btn>
            <v-btn variant="text" color="grey" @click="router.push('/')"> 홈으로 </v-btn>
            <v-spacer></v-spacer>
            <v-btn
              color="primary"
              :disabled="!isFormValid"
              :loading="isSubmitting"
              size="large"
              variant="elevated"
              @click="handleSave"
            >
              변경사항 저장
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
  <v-overlay
    :model-value="isSubmitting"
    class="align-center justify-center"
    persistent
  >
    <v-progress-circular indeterminate color="white" size="64"></v-progress-circular>
  </v-overlay>
</template>
<style scoped>
.v-card {
  border-radius: 12px;
}
.v-toolbar {
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}
</style>
