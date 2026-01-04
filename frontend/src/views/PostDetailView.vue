<script setup>
import { computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePostsStore } from '@/stores/posts'
import { useUiStore } from '@/stores/ui.js'
import { useAuthStore } from '@/stores/auth'
import { useCategoriesStore } from '@/stores/categories'
import { formatDateTime } from '@/utils/formatDate'
import CommentSection from '@/components/CommentSection.vue'
import hljs from 'highlight.js' // highlight.js 임포트 추가

const route = useRoute() // 현재 라우트(주소창) 정보를 가져오기
const router = useRouter()
const postsStore = usePostsStore()
const authStore = useAuthStore()
const uiStore = useUiStore()
const categoriesStore = useCategoriesStore()
// 주소창의 파라미터의 id 값을 가져온다
const postId = computed(() => Number(route.params.id))

// 현재 로그인 사용자가 글 작성자인지 확인
const isAuthorOrAdmin = computed(() => {
  if (!authStore.isLoggedIn || !postsStore.currentPost) {
    return false
  }
  return authStore.isAdmin || authStore.username === postsStore.currentPost.authorUsername
})

// breadcrumbs를 slug 기반으로 재구성
const breadcrumbItems = computed(() => {
  if (!postsStore.currentPost?.categoryPath || !categoriesStore.categories.length) {
    return []
  }

  const categoryPath = postsStore.currentPost.categoryPath

  return categoryPath.map((pathItem, index) => {
    // categoriesStore에서 slug 찾기
    let slug = null
    let parentSlug = null

    if (index === 0) {
      // 부모 카테고리
      const parent = categoriesStore.categories.find((c) => c.id === pathItem.id)
      slug = parent?.slug
    } else {
      // 자식 카테고리
      const parent = categoriesStore.categories.find((c) => c.id === categoryPath[0].id)
      const child = parent?.children.find((c) => c.id === pathItem.id)
      slug = child?.slug
      parentSlug = parent?.slug
    }

    return {
      ...pathItem,
      slug,
      parentSlug,
      disabled: index === 0, // 부모는 비활성화
      to: index === 0 ? null : `/${parentSlug}/${slug}`, // 자식만 링크 제공
    }
  })
})

// highlight.js를 적용하는 로직을 추가
watch(
  () => postsStore.currentPost,
  (newPost) => {
    if (newPost) {
      // DOM이 업데이트 된 후에 highlightAll을 호출
      nextTick(() => {
        document.querySelectorAll('pre code').forEach((block) => {
          hljs.highlightElement(block)
        })
      })
    }
  },
  { deep: true, immediate: true },
)

// 라우트 변경 감지하는
watch(
  postId,
  (newId) => {
    if (newId && !isNaN(newId)) {
      postsStore.fetchPost(newId)
    }
  },
  { immediate: true },
) // 컴포넌트 마운트 시에도 실행

function goBack() {
  router.back()
}

async function removePost() {
  if (confirm('정말로 삭제하시겠습니까?')) {
    const isSuccess = await postsStore.deletePost(postId.value)
    if (isSuccess) {
      uiStore.showSnackbar({ text: '게시글이 삭제되었습니다.', color: 'success' })
      // 현재 URL 정보를 이용해 상세 페이지로 이동
      const { parentSlug, childSlug } = route.params
      if (parentSlug && childSlug) {
        await router.push(`/${parentSlug}/${childSlug}`)
      } else if (parentSlug) {
        // 자식 없는 상위 카테고리 게시글의 경우
        await router.push(`/${parentSlug}`)
      } else {
        await router.push('/') // 그 외 경우 홈으로
      }
    } else {
      uiStore.showSnackbar({ text: '게시글 삭제에 실패했습니다.', color: 'error' })
    }
  }
}
</script>

<template>
  <v-container class="post-content-container">
    <v-card v-if="postsStore.currentPost">
      <!-- breadcrumbs -->
      <v-breadcrumbs v-if="breadcrumbItems.length > 0" :items="breadcrumbItems" class="pa-0 mb-4">
        <template v-slot:item="{ item }">
          <v-breadcrumbs-item :to="item.to" :disabled="item.disabled">
            {{ item.name }}
          </v-breadcrumbs-item>
        </template>
      </v-breadcrumbs>

      <v-card-title class="text-h4">{{ postsStore.currentPost.title }}</v-card-title>
      <v-card-subtitle class="d-flex align-center pt-2 px-4">
        <div>
          <span class="author">작성자: {{ postsStore.currentPost.authorNickname }}</span>
          <span class="separator">|</span>
          <span class="date">작성일: {{ formatDateTime(postsStore.currentPost.createdAt) }}</span>
        </div>

        <v-spacer></v-spacer>

        <div class="text-caption">
          <span class="mr-3">조회 {{ postsStore.currentPost.viewCount }}</span>
          <span class="mr-3">좋아요 {{ postsStore.currentPost.likeCount }}</span>
          <span>댓글 {{ postsStore.currentPost.commentCount }}</span>
        </div>
      </v-card-subtitle>
      <v-divider class="my-4"></v-divider>
      <!-- tiptap 적용후 v-html로 변경 -->
      <v-card-text class="text-body-1">
        <div class="prose-content tiptap-content" v-html="postsStore.currentPost.content"></div>
      </v-card-text>
      <v-card-text class="text-center">
        <v-btn
          :icon="postsStore.currentPost.isLiked ? 'mdi-heart' : 'mdi-heart-outline'"
          :color="postsStore.currentPost.isLiked ? 'red' : 'grey'"
          variant="tonal"
          size="large"
          rounded="circle"
          @click="postsStore.toggleLike(postId)"
        >
        </v-btn>
        <div class="text-caption mt-2">{{ postsStore.currentPost.likeCount }}</div>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="on-primary" @click="goBack" variant="elevated">뒤로가기</v-btn>
        <template v-if="isAuthorOrAdmin">
          <v-btn color="red" @click="removePost" variant="elevated">삭제</v-btn>
          <v-btn color="blue" :to="`${route.path}/edit`" variant="elevated">수정</v-btn>
        </template>
      </v-card-actions>
    </v-card>
    <CommentSection
      v-if="postsStore.currentPost"
      :post-id="postsStore.currentPost.id"
      :total-comments="postsStore.currentPost.commentCount"
      :key="`comment-${postId}`"
    />
    <!-- 로딩 스피너 추가 -->
    <v-row v-else align="center" justify="center" class="my-12">
      <v-progress-circular indeterminate color="primary" size="48" />
    </v-row>
  </v-container>
</template>

<style scoped>
.v-card-text .prose-content h1,
.v-card-text .prose-content h2,
.v-card-text .prose-content h3 {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
}

.author {
  margin-right: 12px;
}
.separator {
  margin-right: 12px;
}
</style>
