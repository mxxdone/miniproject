<script setup>
import { onMounted } from 'vue'
import { useCategoriesStore } from '@/stores/categories.js'

const categoriesStore = useCategoriesStore()

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
    ></v-list-item>

    <v-list-group
      v-for="category in categoriesStore.categories"
      :key="category.id"
      :value="category.name"
    >
      <template v-slot:activator="{ props }">
        <v-list-item
          v-bind="props"
          :title="category.name"
        ></v-list-item>
      </template>

      <v-list-item
        v-for="child in category.children"
        :key="child.id"
        :title="child.name"
        :to="`/?category=${child.id}`"
        class="pl-8"
      ></v-list-item>
    </v-list-group>
  </v-list>
</template>
