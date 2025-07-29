<script setup>
import { computed, onMounted, watch } from 'vue'
import { usePostsStore } from '@/stores/posts'
import { useCategoriesStore } from '@/stores/categories'
import { useRoute, useRouter } from 'vue-router'

// posts.js에 작성한 posts 스토어를 사용
const postsStore = usePostsStore()
const categoriesStore = useCategoriesStore()
const route = useRoute() // 현재 경로 정보 읽기
const router = useRouter() // 경로 변경에 필요

const currentPage = computed(() => Number(route.query.page) || 1)
const selectedCategory = computed({
  get: () => (route.query.category ? Number(route.query.category) : 0),
  set: (value) => {
    selectCategory(value)
  },
})

onMounted(() => {
  categoriesStore.fetchCategories() // 카테고리 목록도 함께 로딩
})

// URL의 쿼리 변경시 데이터 새로 불러오기
watch(
  () => route.query,
  (newQuery) => {
    const pageNumber = Number(newQuery.page) || 1
    const categoryId = newQuery.category ? Number(newQuery.category) : null
    postsStore.fetchPosts({ pageNumber, categoryId })
    // onMounted 이후 중복 호출 방
  },
  // 페이지 처음 진입시에도 즉시 실행, deep: true로 객체 내부 변경 감지
  { immediate: true, deep: true },
)

// 페이지네이션 클릭시 page 쿼리 파라미터 변경
function handlePageChange(newPage) {
  router.push({ query: { ...route.query, page: newPage } })
}

function selectCategory(categoryId) {
  if ((route.query.category ? Number(route.query.category) : 0) === categoryId) return
  const query = { ...route.query }
  if (categoryId) {
    // 카테고리id가 0이 아닐 때 -> '전체'가 아닐때
    query.category = categoryId
  } else {
    delete query.category // '전체'를 누르면 카테고리 파라미터 제거
  }
  query.page = 1 // 카테고리 변경 시 항상 1페이지로
  router.push({ query })
}
</script>

<template>
  <v-container>
    <v-row>
      <v-col>
        <v-chip-group v-model="selectedCategory" mandatory selected-class="text-primary">
          <v-chip :value="0">전체</v-chip>
          <v-chip
            v-for="category in categoriesStore.categories"
            :key="category.id"
            :value="category.id"
          >
            {{ category.name }}
          </v-chip>
        </v-chip-group>
      </v-col>
    </v-row>

    <v-row></v-row>

    <v-row v-if="postsStore.page.totalPages > 0" class="justify-center mt-4"></v-row>
    <v-row>
      <v-col cols="12" class="text-right">
        <v-btn color="primary" to="/posts/new">글쓰기</v-btn>
      </v-col>
    </v-row>
    <v-row>
      <v-col v-for="post in postsStore.posts" :key="post.id" cols="12" sm="6" md="4">
        <RouterLink :to="`/posts/${post.id}`" class="text-decoration-none">
          <v-card class="h-100 d-flex flex-column justify-space-between">
            <div>
              <v-card-title>{{ post.title }}</v-card-title>
              <v-card-text class="content-truncate pb-10">{{ post.content }}</v-card-text>
              <div style="height: 10px"></div>
            </div>
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
