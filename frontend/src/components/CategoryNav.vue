<script setup>
import { onMounted } from 'vue'
import { useCategoriesStore } from '@/stores/categories.js'
import { useRoute } from 'vue-router'

const categoriesStore = useCategoriesStore()
const route = useRoute() // route 객체 이용, 현재 경로 정보 사용.

onMounted(() => {
  categoriesStore.fetchCategories()
})
</script>

<template>
  <v-list density="compact">
    <v-list-item
      prepend-icon="mdi-home"
      title="전체 게시글"
      to="/"
      exact
      :active="!route.query.category"
    ></v-list-item>

    <template v-for="category in categoriesStore.categories" :key="category.id">
      <template v-if="category.name === 'About Me'">
        <v-list-item
          :prepend-icon="'mdi-account-box-outline'"
          :title="category.name"
          to="/about"
        ></v-list-item>
      </template>

      <template v-else>
        <v-list-item
          v-if="!category.children || category.children.length === 0"
          :prepend-icon="'mdi-folder-outline'"
          :title="category.name"
          :to="`/?category=${category.id}`"
          :active="Number(route.query.category) === category.id"
        ></v-list-item>

        <v-list-group v-else :value="category.name">
          <template v-slot:activator="{ props }">
            <v-list-item
              v-bind="props"
              :prepend-icon="'mdi-folder-open-outline'"
              :title="category.name"
            ></v-list-item>
          </template>

          <v-list-item
            v-for="child in category.children"
            :key="child.id"
            :title="child.name"
            :to="`/?category=${child.id}`"
            :active="Number(route.query.category) === child.id"
            class="pl-8"
          ></v-list-item>
        </v-list-group>
      </template>
    </template>
  </v-list>
</template>
