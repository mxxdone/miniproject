<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePostsStore } from '@/stores/posts'
import { useUiStore } from '@/stores/ui'
import { useAuthStore } from '@/stores/auth'
import TiptapEditor from '@/views/TiptapEditor.vue'

const route = useRoute()
const router = useRouter()
const postsStore = usePostsStore()
const uiStore = useUiStore()
const authStore = useAuthStore()

const postId = route.params.id
const form = ref(null)
const title = ref('')
const content = ref('')

// 페이지가 로드될 때 기존 게시글 데이터를 불러옴
onMounted(() => {
  postsStore.fetchPost(postId)
})

// watch(source, callback)
// 스토어의 currentPost 값이 변경(로드 완료)되면, 폼 데이터에 채워 넣음
watch(() => postsStore.currentPost, (newPost) => {
  if (newPost) {
    // 권한 확인
    const isAuthor = authStore.username === newPost.authorUsername
    const isAdmin = authStore.isAdmin

    if (!isAuthor && !isAdmin) {
      uiStore.showSnackbar({ text: '이 게시글을 수정할 권한이 없습니다.', color: 'error'})
      if (window.history.length > 2) {
        // 브라우저 히스토리가 충분히 있는 경우 (앱 내에서 이동)
        router.go(-1)
      } else {
        // 히스토리가 부족한 경우 (직접 URL 접근 또는 외부 링크)
        router.replace(`/posts/${postId}`) // 게시글 상세로
      }
      return
    }

    //권한이 있는 경우에 폼 데이터 채우기
    title.value = newPost.title
    content.value = newPost.content
  }
})

const rules = {
  required: value => !!value || '필수 항목입니다.',
  minLength: value => value.length >= 2 || '2글자 이상 입력해 주세요.',
}

async function submitUpdate() {
  const { valid } = await form.value.validate()
  if (!content.value || content.value === '<p></p>') {
    uiStore.showSnackbar({ text: '내용을 입력해주세요.', color: 'error' })
    return
  }

  if (valid) {
    const isSuccess = await postsStore.updatePost(postId, {
      title: title.value,
      content: content.value
    })
    if (isSuccess) {
      uiStore.showSnackbar({ text: '게시글이 성공적으로 수정되었습니다.', color: 'success' })
      await router.push(`/posts/${postId}`) // 수정된 상세 페이지로 이동
    } else {
      uiStore.showSnackbar({ text: '게시글 수정에 실패했습니다.', color: 'error' })
    }
  }
}
</script>

<template>
  <v-container>
    <v-card v-if="postsStore.currentPost">
      <v-card-title class="text-h5">게시글 수정</v-card-title>
      <v-card-text>
        <v-form ref="form" @submit.prevent="submitUpdate">
          <v-text-field
            v-model="title"
            label="제목"
            :rules="[rules.required, rules.minLength]"
            required
          ></v-text-field>
          <div class="text-subtitle-1 font-weight-medium mb-2">내용</div>
          <TiptapEditor v-model:content="content" />
          <v-btn color="primary" to="/" class="mt-4 mr-2">뒤로가기</v-btn>
          <v-btn type="submit" color="primary" class="mt-4">수정</v-btn>
        </v-form>
      </v-card-text>
    </v-card>
    <v-row v-else align="center" justify="center" class="my-12">
      <v-progress-circular indeterminate color="primary" size="48" />
    </v-row>
  </v-container>
</template>
