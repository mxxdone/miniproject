<script setup>
import { onMounted } from 'vue'
import { usePostsStore } from '@/stores/posts'

// posts 스토어를 사용
const postsStore = usePostsStore()

// 이 컴포넌트가 화면에 마운트(표시)되면 fetchPosts 액션을 실행
onMounted(() => {
  postsStore.fetchPosts()
})
</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="12" class="text-right">
        <v-btn color="primary" to="/posts/new">글쓰기</v-btn>
      </v-col>
    </v-row>
    <v-row>
      <v-col
        v-for="post in postsStore.posts"
        :key="post.id"
        cols="12"
        sm="6"
        md="4"
      >
        <RouterLink :to="`/posts/${post.id}`" class="text-decoration-none">
          <v-card>
            <v-card-title>{{ post.title }}</v-card-title>
            <v-card-text>{{ post.content }}</v-card-text>
          </v-card>
        </RouterLink>
      </v-col>
    </v-row>
  </v-container>
</template>
