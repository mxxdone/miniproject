<!-- frontend/src/components/TiptapToolbar.vue -->
<script setup>
const props = defineProps({
  editor: { type: Object, required: true },
  fileInputRef: { type: [Object, null], required: true },
})

function openFilePicker() {
  // ref는 템플릿 ref 객체이므로 .value로 접근
  props.fileInputRef?.value?.click()
}
</script>

<template>
  <div class="toolbar" v-if="editor">
    <v-btn
      icon="mdi-format-bold"
      size="small"
      variant="text"
      @click="editor.chain().focus().toggleBold().run()"
      :class="{ 'is-active': editor.isActive('bold') }"
    />
    <v-btn
      icon="mdi-format-italic"
      size="small"
      variant="text"
      @click="editor.chain().focus().toggleItalic().run()"
      :class="{ 'is-active': editor.isActive('italic') }"
    />
    <v-btn
      icon="mdi-format-strikethrough"
      size="small"
      variant="text"
      @click="editor.chain().focus().toggleStrike().run()"
      :class="{ 'is-active': editor.isActive('strike') }"
    />

    <v-btn
      icon="mdi-table-plus"
      size="small"
      variant="text"
      @click="editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()"
    />

    <v-menu v-if="editor.isActive('table')" offset="5">
      <template #activator="{ props: menuProps }">
        <v-btn
          v-bind="menuProps"
          icon="mdi-table-edit"
          size="small"
          variant="tonal"
          color="primary"
          class="ml-2"
        />
      </template>

      <v-list density="compact">
        <v-list-item @click="editor.chain().focus().addColumnAfter().run()">
          <template #prepend><v-icon icon="mdi-table-column-plus-after" /></template>
          <v-list-item-title>오른쪽에 열 추가</v-list-item-title>
        </v-list-item>
        <v-list-item @click="editor.chain().focus().addRowAfter().run()">
          <template #prepend><v-icon icon="mdi-table-row-plus-after" /></template>
          <v-list-item-title>아래에 행 추가</v-list-item-title>
        </v-list-item>
        <v-divider />
        <v-list-item @click="editor.chain().focus().deleteColumn().run()" color="error">
          <template #prepend><v-icon icon="mdi-table-column-remove" /></template>
          <v-list-item-title>현재 열 삭제</v-list-item-title>
        </v-list-item>
        <v-list-item @click="editor.chain().focus().deleteRow().run()" color="error">
          <template #prepend><v-icon icon="mdi-table-row-remove" /></template>
          <v-list-item-title>현재 행 삭제</v-list-item-title>
        </v-list-item>
        <v-divider />
        <v-list-item @click="editor.chain().focus().deleteTable().run()" color="red">
          <template #prepend><v-icon icon="mdi-delete-forever" /></template>
          <v-list-item-title>표 전체 삭제</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>

    <v-btn icon="mdi-image-plus" size="small" variant="text" @click="openFilePicker" />
    <v-btn icon="mdi-youtube" size="small" variant="text" @click="$emit('insert-youtube')" />
  </div>
</template>

<style scoped>
.toolbar {
  padding: 8px;
  border-bottom: 1px solid #ccc;
  background-color: #f5f5f5;
}
.is-active {
  background-color: rgba(0, 0, 0, 0.1);
}
</style>
