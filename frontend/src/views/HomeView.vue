<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { usePostsStore } from '@/stores/posts'
import { useAuthStore } from '@/stores/auth'
import { useRoute, useRouter } from 'vue-router'
import { stripHtml } from "@/utils/formatText";

// posts.js에 작성한 posts 스토어를 사용
const postsStore = usePostsStore()
const authStore = useAuthStore()
const route = useRoute() // 현재 경로 정보 읽기
const router = useRouter() // 경로 변경에 필요

// 검색 UI와 연결될 로컬 상태
const searchType = ref('all')
const searchKeyword = ref('')
const searchTypes = [
  { title: '전체', value: 'all' },
  { title: '제목', value: 'title' },
  { title: '내용', value: 'content' },
]

const currentPage = computed(() => Number(route.query.page) || 1)
const currentCategory = computed(() => (route.query.category ? Number(route.query.category) : null))
const processedPosts = computed(() => {
  // postsStore.posts가 변경될 때만 이 부분이 다시 계산됩니다.
  return postsStore.posts.map(post => ({
    ...post, // 기존 post 객체의 모든 속성을 복사
    // HTML 태그가 제거된 순수 텍스트를 새로운 속성으로 추가
    plainContent: stripHtml(post.content)
  }))
})

onMounted(() => {
  // URL 쿼리에서 검색어 상태 복원
  searchType.value = route.query.type || 'all'
  searchKeyword.value = route.query.keyword || ''
})

// URL의 쿼리 변경시 데이터 새로 불러오기
watch(
  () => route.query,
  (newQuery, oldQuery) => {
    if (oldQuery && newQuery.category !== oldQuery.category) {
      searchKeyword.value = '';
      searchType.value = 'all';
    }

    const pageNumber = Number(newQuery.page) || 1
    const categoryId = newQuery.category ? Number(newQuery.category) : null
    const type = newQuery.type || 'all'
    const keyword = newQuery.keyword || ''

    postsStore.fetchPosts({ pageNumber, categoryId, type, keyword })
    // onMounted 이후 중복 호출 방지
  },
  // immediate: 페이지 처음 진입시에도 즉시 실행
  // deep: true로 객체 내부 변경 감지
  { immediate: true, deep: true },
)

// 페이지네이션 클릭시 page 쿼리 파라미터 변경
function handlePageChange(newPage) {
  console.log(newPage);
  router.push({ query: { ...route.query, page: newPage } })
}

function handleSearch() {
  router.push({
    query: {
      ...route.query, // 현재 카테고리 유지
      type: searchType.value,
      keyword: searchKeyword.value,
      page: 1 // 검색 시 항상 1페이지로
    }
  })
}
</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card class="pa-4">
          <v-row align="center" no-gutters>
            <v-col cols="3" class="pr-2">
              <v-select v-model="searchType" :items="searchTypes" density="compact" hide-details></v-select>
            </v-col>
            <v-col cols="7">
              <v-text-field v-model="searchKeyword" placeholder="검색어를 입력하세요" density="compact" hide-details @keyup.enter="handleSearch"></v-text-field>
            </v-col>
            <v-col cols="2" class="pl-2">
              <v-btn color="primary" block @click="handleSearch">검색</v-btn>
            </v-col>
          </v-row>
        </v-card>
      </v-col>
    </v-row>
    <v-row>
      <v-col class="d-flex justify-end">
        <v-btn
          v-if="currentCategory && authStore.isAdmin"
          color="primary"
          :to="`/posts/new?categoryId=${currentCategory}`"
        >
          글쓰기
        </v-btn>
      </v-col>
    </v-row>
    <v-row>
      <v-col v-for="post in processedPosts" :key="post.id" cols="12" sm="6" md="4">
        <RouterLink :to="`/posts/${post.id}`" class="text-decoration-none">
          <v-card class="h-100 d-flex flex-column justify-space-between">
            <div>
              <v-card-title>{{ post.title }}</v-card-title>
              <v-card-text class="content-truncate pb-10">
                <div v-html="post.plainContent"></div>
              </v-card-text>
              <div style="height: 10px"></div>
            </div>
            <v-card-actions class="px-4">
              <v-icon size="small">mdi-comment-text-outline</v-icon>
              <span class="text-caption ml-1">{{ post.commentCount }}</span>
            </v-card-actions>
          </v-card>
        </RouterLink>
      </v-col>
    </v-row>
    <v-row v-if="postsStore.page.totalPages > 0" class="justify-center mt-4">
      <v-col cols="auto">
        <v-pagination
          :model-value="currentPage"
          :length="postsStore.page.totalPages"
          :total-visible="7"
          @update:modelValue="handlePageChange"
        >
        </v-pagination>
      </v-col>
    </v-row>
  </v-container>
</template>
<style scoped>
.content-truncate {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
  max-height: calc(1.5em * 3);
}
</style>
