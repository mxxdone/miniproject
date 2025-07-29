<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePostsStore } from '@/stores/posts'
import { useUiStore } from "@/stores/ui.js"

const route = useRoute() // 현재 라우트(주소창) 정보를 가져오기
const router = useRouter()
const postsStore = usePostsStore()
const uiStore = useUiStore()
// 주소창의 파라미터의 id 값을 가져온다
const postId = route.params.id

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
    } else {
      uiStore.showSnackbar({ text: '게시글 삭제에 실패했습니다. 잠시 후 다시 시도해 주세요.', color: 'error' })
    }
  }
}
</script>

<template>
  <v-container>
    <v-card v-if="postsStore.currentPost">
      <v-chip color="primary" class="mb-4">{{ postsStore.currentPost.categoryName }}</v-chip>
      <v-card-title class="text-h4">{{ postsStore.currentPost.title }}</v-card-title>
      <v-card-subtitle>
        작성일: {{ postsStore.currentPost.createdAt }}
      </v-card-subtitle>
      <v-divider class="my-4"></v-divider>
      <v-card-text class="text-body-1" style="white-space: pre-wrap;">
        {{ postsStore.currentPost.content }}
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="goBack">뒤로가기</v-btn>
        <v-btn color="blue" :to="`/posts/${postId}/edit`">수정</v-btn>
        <v-btn color="red" @click="removePost">삭제</v-btn>
      </v-card-actions>
    </v-card>
    <!-- 로딩 스피너 추가 -->
    <v-row v-else align="center" justify="center" class="my-12">
      <v-progress-circular indeterminate color="primary" size="48" />
    </v-row>
<!--    <div v-else>
      <p>게시글을 불러오는 중...</p>
    </div>-->
  </v-container>
</template>
