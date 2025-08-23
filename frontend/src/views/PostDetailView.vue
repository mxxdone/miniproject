<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePostsStore } from '@/stores/posts'
import { useUiStore } from '@/stores/ui.js'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/formatDate'
import CommentSection from '@/components/CommentSection.vue'

const route = useRoute() // 현재 라우트(주소창) 정보를 가져오기
const router = useRouter()
const postsStore = usePostsStore()
const authStore = useAuthStore()
const uiStore = useUiStore()
// 주소창의 파라미터의 id 값을 가져온다
const postId = route.params.id

// 현재 로그인 사용자가 글 작성자인지 확인
const isAuthor = computed(() => {
  return (
    authStore.isLoggedIn &&
    postsStore.currentPost &&
    authStore.username === postsStore.currentPost.authorUsername
  )
})

onMounted(() => {
  // 주소창에서 id 값을 가져와서 fetchPost 액션을 호출
  postsStore.fetchPost(postId)
})

function goBack() {
  router.back()
}

async function removePost() {
  if (confirm('정말로 삭제하시겠습니까?')) {
    const isSuccess = await postsStore.deletePost(postId)
    if (isSuccess) {
      uiStore.showSnackbar({ text: '게시글이 삭제되었습니다.', color: 'success' })
      await router.push('/')
    }
  }
}
</script>

<template>
  <v-container>
    <v-card v-if="postsStore.currentPost">
      <v-breadcrumbs :items="postsStore.currentPost.categoryPath" class="pa-0 mb-4">
        <template v-slot:item="{ item }">
          <v-breadcrumbs-item :to="`/?category=${item.id}`" :disabled="item.disabled">
            {{ item.name }}
          </v-breadcrumbs-item>
        </template>
      </v-breadcrumbs>

      <v-card-title class="text-h4">{{ postsStore.currentPost.title }}</v-card-title>
      <v-card-subtitle>
        작성자: {{ postsStore.currentPost.authorUsername }} | 작성일:
        {{ formatDateTime(postsStore.currentPost.createdAt) }}
      </v-card-subtitle>
      <v-divider class="my-4"></v-divider>
      <!-- tiptap 적용후 v-html로 변경 -->
      <v-card-text class="text-body-1">
        <div v-html="postsStore.currentPost.content"></div>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="goBack">뒤로가기</v-btn>
        <template v-if="isAuthor">
          <v-btn color="red" @click="removePost">삭제</v-btn>
          <v-btn color="blue" :to="`/posts/${postId}/edit`">수정</v-btn>
        </template>
      </v-card-actions>
    </v-card>
    <CommentSection v-if="postsStore.currentPost" :post-id="postsStore.currentPost.id" />
    <!-- 로딩 스피너 추가 -->
    <v-row v-else align="center" justify="center" class="my-12">
      <v-progress-circular indeterminate color="primary" size="48" />
    </v-row>
  </v-container>
</template>
