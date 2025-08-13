<script setup>
import { computed, onMounted } from 'vue'
import { useCategoriesStore } from '@/stores/categories.js'
import { usePostsStore } from '@/stores/posts'
import { useRoute } from 'vue-router'

const categoriesStore = useCategoriesStore()
const postsStore = usePostsStore()
const route = useRoute()

onMounted(() => {
  categoriesStore.fetchCategories()
})

// 현재 활성화된 카테고리 ID
const activeCategoryId = computed(() => {
  if (route.name === 'postDetail' && postsStore.currentPost) {
    return postsStore.currentPost.categoryId
  }
  return route.query.category ? Number(route.query.category) : null
})
</script>

<template>
  <v-list nav density="compact">
    <v-list-item
      prepend-icon="mdi-home"
      title="전체 게시글"
      to="/"
      exact
      :active="!activeCategoryId && route.name === 'home'"
    />

    <template v-for="category in categoriesStore.categories" :key="category.id">
      <v-list-item
        v-if="category.name === 'About Me'"
        prepend-icon="mdi-account-box-outline"
        to="/about"
        :active="route.name === 'aboutMe'"
      >
        <v-list-item-title> About Me </v-list-item-title>
      </v-list-item>

      <div v-else>
        <v-list-item
          :prepend-icon="'mdi-folder-open-outline'"
          :to="`/?category=${category.id}`"
          :active="activeCategoryId === category.id"
        >
          <v-list-item-title class="text-subtitle-1 font-weight-medium">
            {{ category.name }}
          </v-list-item-title>
        </v-list-item>

        <v-list-item
          v-for="child in category.children"
          :key="child.id"
          :to="`/?category=${child.id}`"
          :active="activeCategoryId === child.id"
        >
          <v-list-item-title class="ms-6">
            {{ child.name }}
          </v-list-item-title>
        </v-list-item>
      </div>
    </template>
  </v-list>
</template>
