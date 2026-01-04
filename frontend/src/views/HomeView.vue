<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { usePostsStore } from '@/stores/posts'
import { useAuthStore } from '@/stores/auth'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { stripHtml } from '@/utils/formatText'
import { useCategoriesStore } from '@/stores/categories'

// posts.js에 작성한 posts 스토어를 사용
const postsStore = usePostsStore()
const authStore = useAuthStore()
const route = useRoute() // 현재 경로 정보 읽기
const router = useRouter() // 경로 변경에 필요
const categoriesStore = useCategoriesStore()

// 검색 UI와 연결될 로컬 상태
const searchType = ref('all')
const searchKeyword = ref('')
const searchTypes = [
  { title: '전체', value: 'all' },
  { title: '제목', value: 'title' },
  { title: '내용', value: 'content' },
]

const currentPage = computed(() => Number(route.query.page) || 1)

// '글쓰기' 버튼에 현재 카테고리 ID를 전달하기 위한 computed
const currentCategory = computed(() => {
  const { parentSlug, childSlug } = route.params
  if (!parentSlug) return null
  if (childSlug) {
    const parent = categoriesStore.categories.find((c) => c.slug === parentSlug)
    const child = parent?.children.find((c) => c.slug === childSlug)
    return child?.id || null
  }
  const parent = categoriesStore.categories.find((c) => c.slug === parentSlug)
  return parent?.id || null
})

// Breadcrumb과 유사한 카테고리 경로 텍스트
const categoryPathText = computed(() => {
  const { parentSlug, childSlug } = route.params
  if (!parentSlug) return ''
  const parent = categoriesStore.categories.find((c) => c.slug === parentSlug)
  if (!parent) return ''
  if (childSlug) {
    const child = parent.children?.find((c) => c.slug === childSlug)
    return child ? `${parent.name} > ${child.name}` : parent.name
  }
  return parent.name
})

// 목록에서는 HTML 태그를 제거하여 순수 텍스트만 보여주기 위한 computed
const processedPosts = computed(() => {
  // postsStore.posts가 변경될 때만 이 부분이 다시 계산됩니다.
  return postsStore.posts.map((post) => ({
    ...post, // 기존 post 객체의 모든 속성을 복사
    plainContent: stripHtml(post.content),
  }))
})

onMounted(async () => {
  searchType.value = route.query.type || 'all'
  searchKeyword.value = route.query.keyword || ''
  await categoriesStore.fetchCategories()
  if (categoriesStore.categories.length > 0) {
    fetchPostsWithCategory()
  }
})

// 게시글 조회 로직을 함수로 분리
function fetchPostsWithCategory() {
  const { parentSlug, childSlug } = route.params
  if (categoriesStore.categories.length === 0) {
    return
  }

  let categoryId = null

  if (childSlug) {
    const parent = categoriesStore.categories.find((c) => c.slug === parentSlug)
    const child = parent?.children.find((c) => c.slug === childSlug)
    categoryId = child?.id
  } else if (parentSlug) {
    const parent = categoriesStore.categories.find((c) => c.slug === parentSlug)
    categoryId = parent?.id
  }

  const pageNumber = Number(route.query.page) || 1
  const type = route.query.type || 'all'
  const keyword = route.query.keyword || ''

  postsStore.fetchPosts({ pageNumber, categoryId, type, keyword })
}

// categories와 route를 함께 감시하여 데이터 동기화
watch(
  [() => route.params, () => route.query, () => categoriesStore.categories.length],
  () => {
    if (categoriesStore.categories.length > 0) {
      fetchPostsWithCategory()
    }
  },
  { deep: true },
)

// 페이지네이션 클릭시 page 쿼리 파라미터 변경
function handlePageChange(newPage) {
  router.push({ query: { ...route.query, page: newPage } })
}

// 검색 버튼 클릭 시 검색 관련 쿼리 파라미터 변경
function handleSearch() {
  router.push({
    query: {
      ...route.query,
      type: searchType.value,
      keyword: searchKeyword.value,
      page: 1,
    },
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
              <v-select
                v-model="searchType"
                :items="searchTypes"
                density="compact"
                hide-details
              ></v-select>
            </v-col>
            <v-col cols="7">
              <v-text-field
                v-model="searchKeyword"
                placeholder="검색어를 입력하세요"
                density="compact"
                hide-details
                @keyup.enter="handleSearch"
              ></v-text-field>
            </v-col>
            <v-col cols="2" class="pl-2">
              <v-btn color="primary" block @click="handleSearch">검색</v-btn>
            </v-col>
          </v-row>
        </v-card>
      </v-col>
    </v-row>
    <v-row v-if="categoryPathText">
      <v-col cols="8">
        <div class="text-h6 text-grey-darken-1 mb-4">
          {{ categoryPathText }}
        </div>
      </v-col>
      <v-col class="d-flex justify-end">
        <v-btn
          v-if="currentCategory && authStore.isAdmin"
          color="primary"
          :to="`/posts/new?category=${currentCategory}`"
        >
          글쓰기
        </v-btn>
      </v-col>
    </v-row>
    <v-row class="posts-grid">
      <v-col v-for="post in processedPosts" :key="post.id" cols="12" sm="6" md="4">
        <RouterLink
          :to="`/${post.parentSlug}/${post.childSlug}/posts/${post.id}`"
          class="text-decoration-none text-black"
        >
          <v-card class="post-card h-100 d-flex flex-column">
            <div>
              <v-card-title>{{ post.title }}</v-card-title>
              <v-card-text class="content-truncate pd-2">
                <div>{{ post.plainContent }}</div>
              </v-card-text>
              <div style="height: 4px"></div>
            </div>
            <v-card-actions class="px-4 mt-auto">
              <v-icon size="small">mdi-heart-outline</v-icon>
              <span class="mr-3">{{ post.likeCount }}</span>
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
  min-height: calc(1.5em * 3);
  max-height: calc(1.5em * 3);
}
.posts-grid .post-card {
  border: 1px solid rgba(0, 0, 0, 0.06) !important;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04) !important;
  transition:
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.posts-grid .post-card:hover {
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
</style>
