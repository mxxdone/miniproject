<script setup>
import { watch } from 'vue'
import { usePostsStore } from '@/stores/posts'

// posts.js에 작성한 posts 스토어를 사용
const postsStore = usePostsStore()

//컴포넌트가 마운트될 떄 감시 대상의 현재 값으로 watch 콜백 즉시 한 번 실행
watch(
  () => postsStore.page.currentPage,
  (newPage) => {
    postsStore.fetchPosts(newPage)
  },
  { immediate: true },
)
</script>

<template>
  <v-container>
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
        ></v-pagination>
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
