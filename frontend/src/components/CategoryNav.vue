<script setup>
import { onMounted, ref, watch } from 'vue'
import { useCategoriesStore } from '@/stores/categories.js'
import { useRoute } from 'vue-router'

const categoriesStore = useCategoriesStore()
const route = useRoute()
const opened = ref([])

onMounted(() => {
  categoriesStore.fetchCategories()
})

// 자식 카테고리가 active 상태인지 확인하는 함수
function isChildActive(category, child) {
  const currentPath = route.path
  const categoryPath = `/${category.slug}/${child.slug}`

  // 카테고리 목록 페이지 또는 글 상세보기 페이지
  if (currentPath.startsWith(categoryPath)) {
    return true
  }

  // 글쓰기 페이지: query로 카테고리 확인
  if (route.query.category && Number(route.query.category) === child.id) {
    return true
  }

  return false
}

// About Me 페이지가 active 상태인지 확인하는 함수 추가
function isAboutMeActive() {
  return route.path.startsWith('/about-me')
}

// route.fullPath를 감시하여 params와 query 변경을 감지
watch(
  () => route.fullPath,
  () => {
    if (categoriesStore.categories.length === 0) return

    const { parentSlug } = route.params
    const categoryIdFromQuery = route.query.category ? Number(route.query.category) : null

    let parentToOpen = null

    // case1: 상세/목록 페이지(/...slug...)를 위한 로직
    if (parentSlug) {
      parentToOpen = categoriesStore.categories.find((c) => c.slug === parentSlug)
    }
    // case2: 작성 페이지(/posts/new?category=...)를 위한 로직
    else if (categoryIdFromQuery) {
      // 쿼리로 받은 카테고리 ID로 부모 카테고리 찾기
      for (const category of categoriesStore.categories) {
        if (
          category.children &&
          category.children.some((child) => child.id === categoryIdFromQuery)
          //.some()으로 그 자식 중 하나라도 id가 쿼리에서 온 categoryIdFromQuery와 일치하면 true 반환
        ) {
          parentToOpen = category
          break
        }
      }
    }

    // 최종적으로 열어야 할 부모를 결정합니다.
    if (parentToOpen) {
      opened.value = [parentToOpen.id]
    } else {
      opened.value = [] // 홈 화면 등 해당 사항 없으면 모두 닫습니다.
    }
  },
  { immediate: true },
)
</script>

<template>
  <v-list nav density="compact" v-model:opened="opened">
    <template v-for="category in categoriesStore.categories" :key="category.id">
      <v-list-item
        v-if="category.slug === 'about-me'"
        prepend-icon="mdi-account-box-outline"
        :to="`/${category.slug}`"
        :active="isAboutMeActive()"
      >
        <v-list-item-title> ABOUT ME </v-list-item-title>
      </v-list-item>

      <v-list-group
        v-else-if="category.children && category.children.length > 0"
        :value="category.id"
      >
        <template v-slot:activator="{ props }">
          <v-list-item v-bind="props" :prepend-icon="'mdi-folder-open-outline'">
            <v-list-item-title class="text-subtitle-1 font-weight-medium">
              {{ category.name }}
            </v-list-item-title>
            <template v-slot:append>
              <v-chip size="x-small" variant="tonal" class="ml-2">{{ category.postCount }}</v-chip>
            </template>
          </v-list-item>
        </template>

        <v-list-item
          v-for="child in category.children"
          :key="child.id"
          :to="`/${category.slug}/${child.slug}`"
          :active="isChildActive(category, child)"
        >
          <v-list-item-title class="ms-6">
            {{ child.name }}
          </v-list-item-title>
          <template v-slot:append>
            <v-chip size="x-small" variant="tonal">{{ child.postCount }}</v-chip>
          </template>
        </v-list-item>
      </v-list-group>
    </template>
  </v-list>
</template>
