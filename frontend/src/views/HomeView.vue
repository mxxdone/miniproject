<script setup>
import { onMounted, watch } from 'vue'
import { usePostsStore } from '@/stores/posts'
import { useCategoriesStore } from '@/stores/categories'

// posts.js에 작성한 posts 스토어를 사용
const postsStore = usePostsStore()
const categoriesStore = useCategoriesStore()

onMounted(() => {
  postsStore.fetchPosts({ pageNumber: 1 }) // 초기 로딩은 전체 게시글
  categoriesStore.fetchCategories() // 카테고리 목록도 함께 로딩
})

// 페이지가 변경될 때 현재 선택 카테고리를 유지하며 데이터 요청
watch(
  () => postsStore.page.currentPage,
  (newPage, oldPage) => {
    // onMounted 이후 중복 호출 방지
    if (oldPage != 0) {
      postsStore.fetchPosts({ pageNumber: newPage, categoryId: postsStore.currentCategoryId })
    }
  },
)
// 카테고리 클릭 시 실행될 함수
function selectCategory(categoryId) {
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
