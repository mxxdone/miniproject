<script setup>
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'

// v-model로 content를 받을 수 있도록 설정
const content = defineModel('content');

const editor = useEditor({
  content: content.value,
  extensions: [
    StarterKit, // 기본서식
  ],
  // 내용이 변경될 때마다 v-model 업데이트
  onUpdate: () => {
    // 현재 에디터 내용을 HTML로 변환해 v-model에 반영
    content.value = editor.value.getHTML()
  },
})
</script>

<template>
  <div v-if="editor" class="editor-container">
    <div class="toolbar">
      <v-btn
        icon="mdi-format-bold"
        size="small"
        variant="text"
        @click="editor.chain().focus().toggleBold().run()"
        :class="{ 'is-active': editor.isActive('bold') }"
      ></v-btn>
      <v-btn
        icon="mdi-format-italic"
        size="small"d
        variant="text"
        @click="editor.chain().focus().toggleItalic().run()"
        :class="{ 'is-active': editor.isActive('italic') }"
      ></v-btn>
      <v-btn
        icon="mdi-format-strikethrough"
        size="small"
        variant="text"
        @click="editor.chain().focus().toggleStrike().run()"
        :class="{ 'is-active': editor.isActive('strike') }"
      ></v-btn>
    </div>

    <!-- 편집 영역 -->
    <EditorContent :editor="editor" class="editor-content" />
  </div>
</template>

<style scoped>
.editor-container {
  border: 1px solid #ccc;
  border-radius: 4px;
}
.toolbar {
  padding: 8px;
  border-bottom: 1px solid #ccc;
  background-color: #f5f5f5;
}
.editor-content :deep(.ProseMirror) {
  padding: 16px;
  min-height: 400px;
}
.editor-content :deep(.ProseMirror:focus) {
  outline: none;
}
.is-active {
  background-color: rgba(0, 0, 0, 0.1);
}
</style>
