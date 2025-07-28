<script setup>
import { onMounted, watch } from 'vue'
import { usePostsStore } from '@/stores/posts'
import { useCategoriesStore } from '@/stores/categories'
import { useRoute, useRouter } from 'vue-router'

// posts.js에 작성한 posts 스토어를 사용
const postsStore = usePostsStore()
const categoriesStore = useCategoriesStore()
const route = useRoute(); // 현재 경로 정보 읽기
const router = useRouter(); // 경로 변경에 필요

onMounted(() => {
  categoriesStore.fetchCategories() // 카테고리 목록도 함께 로딩
})

// URL의 쿼리 파라미터(?category=...) 변경시 데이터 새로 불러오기
watch(
  () => route.query.category,
  (newCategoryId) => {
    postsStore.fetchPosts({ pageNumber:1, categoryId: newCategoryId })
    // onMounted 이후 중복 호출 방
  },
  // 페이지 처음 진입시에도 즉시 실행
  { immediate: true }
)

// 페이지 번호가 바뀔 때 데이터 새로 불러오기
watch(() => postsStore.page.currentPage, (newPage, oldPage) => {
  if (oldPage !== 0) {
    postsStore.fetchPosts({ pageNumber: newPage, categoryId: route.query.category})
  }
})
// 카테고리 클릭 시 실행될 함수, URL을 변경함
// categoryId가 falase라면 (null, undefined, 0, '') 빈객체 넘기기
function selectCategory(categoryId) {
  const query = categoryId ? { category: categoryId } : {}
  router.push({ path: '/', query: query })
  postsStore.fetchPosts({ pageNumber: 1, categoryId: categoryId })
}
</script>

<template>
  <v-container>
    <v-row>
      <v-col>
        <v-chip-group mandatory selected-class="text-primary">
          <v-chip @click="selectCategory(null)">전체</v-chip>
          <v-chip
            v-for="category in categoriesStore.categories"
            :key="category.id"
            @click="selectCategory(category.id)"
          >
            {{ category.name }}
          </v-chip>
        </v-chip-group>
      </v-col>
    </v-row>

    <v-row> </v-row>

    <v-row v-if="postsStore.page.totalPages > 0" class="justify-center mt-4"> </v-row>
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
          v-model="postsStore.page.currentPage"
          :length="postsStore.page.totalPages"
          :total-visible="7"
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
