<script setup>
import { EditorContent, useEditor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import { watch } from 'vue'
import Image from '@tiptap/extension-image'
import apiClient from '@/api' // apiClient 추가
import { createLowlight } from 'lowlight'
import CodeBlockLowlight from '@tiptap/extension-code-block-lowlight'

import java from 'highlight.js/lib/languages/java.js'
import javascript from 'highlight.js/lib/languages/javascript.js'
import css from 'highlight.js/lib/languages/css.js'
import sql from 'highlight.js/lib/languages/sql.js'
import xml from 'highlight.js/lib/languages/xml.js'
import html from 'highlight.js/lib/languages/xml.js' // HTML을 XML로 처리
import json from 'highlight.js/lib/languages/json.js'
import yaml from 'highlight.js/lib/languages/yaml.js'
import typescript from 'highlight.js/lib/languages/typescript.js'

const lowlight = createLowlight()
lowlight.register('java', java)
lowlight.register('javascript', javascript)
lowlight.register('css', css)
lowlight.register('sql', sql)
lowlight.register('xml', xml)
lowlight.register('json', json)
lowlight.register('yaml', yaml)
lowlight.register('typescript', typescript)
lowlight.register('html', html)

// v-model로 content를 받을 수 있도록 설정
const content = defineModel('content')

async function handleImageUpload(file) {
  const formData = new FormData()
  formData.append('image', file)

  try {
    const response = await apiClient.post('/api/v1/images/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data // 업로드된 이미지 URL 반환
  } catch (error) {
    console.error('이미지 업로드 실패: ', error)
    return null
  }
}

const editor = useEditor({
  content: content.value,
  extensions: [
    // StarterKit의 기본 codeBlock을 끄고, lowlight 버전으로 새로 추가
    StarterKit.configure({
      codeBlock: false,
    }),
    // configure에 생성된 lowlight 인스턴스 전달 및 설정 추가
    CodeBlockLowlight.configure({
      lowlight,
      languageClassPrefix: 'language-', // 클래스 접두사를 추가
    }),
    Image, // 이미지 확장팩 추가
  ],
  editorProps: {
    // 에디터 속성 추가 (이미지 처리)
    attributes: {
      class: 'prose prose-sm sm:prose-base max-w-none focus:outline-none',
    },
    handlePaste(view, event) {
      const items = event.clipboardData?.items
      if (!items) return

      for (const item of items) {
        if (item.type.indexOf('image') === 0) {
          event.preventDefault()
          const file = item.getAsFile()
          if (file) {
            handleImageUpload(file).then((url) => {
              if (url) {
                editor.value.chain().focus().setImage({ src: url }).run()
              }
            })
          }
        }
      }
    },
    handleDrop(view, event) {
      const files = event.dataTransfer?.files
      if (!files || files.length === 0) return

      const file = files[0]
      if (file.type.startsWith('image/')) {
        event.preventDefault() // 이미지일 때만 기본 동작 막기
        handleImageUpload(file).then((url) => {
          if (url) {
            editor.value.chain().focus().setImage({ src: url }).run()
          }
        })
      }
    },
  },
  // 내용이 변경될 때마다 v-model 업데이트
  onUpdate: () => {
    // 현재 에디터 내용을 HTML로 변환해 v-model에 반영
    content.value = editor.value.getHTML()
  },
})

watch(content, (newContent) => {
  const isSame = editor.value.getHTML() === newContent
  if (isSame) {
    return
  }
  editor.value.commands.setContent(newContent, false)
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
        size="small"
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
