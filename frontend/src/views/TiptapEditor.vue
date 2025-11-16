<script setup>
import { EditorContent, useEditor } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import { ref, watch } from 'vue'
import Image from '@tiptap/extension-image'
import Youtube from '@tiptap/extension-youtube'
import apiClient from '@/api'
import { useUiStore } from '@/stores/ui'

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
// 파일 입력 참조를 위한 ref
const fileInput = ref(null)
const uiStore = useUiStore()

// 이미지 파일인지 확인
const isImageFile = (file) => file && file.type.startsWith('image/')

// 유튜브 URL 판별
const isYoutubeUrl = (text) => {
  if (!text) return false
  return text.includes('youtube.com/watch') || text.includes('youtu.be/')
}

async function handleImageUpload(file) {
  // 파일 업로드 전 이미지 형식 확인
  if (!isImageFile(file)) {
    uiStore.showSnackbar({ text: '이미지 파일만 업로드할 수 있습니다.', color: 'error' })
    return null
  }
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
    uiStore.showSnackbar({ text: '이미지 업로드에 실패했습니다.', color: 'error' })
    return null
  }
}

//  에디터에 이미지 삽입
function insertImage(url) {
  if (url && editor.value) {
    editor.value.chain().focus().setImage({ src: url }).run()
  }
}

// 파일 선택 버튼을 통해 파일이 변경되었을 때 처리
function onFileChange(event) {
  const file = event.target.files[0]
  if (file) {
    handleImageUpload(file).then(insertImage)
  }
  // 동일한 파일을 다시 선택해도 change 이벤트가 발생하도록 값을 초기화
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

function insertYoutubeByPrompt() {
  if (!editor.value) return
  const url = window.prompt('YouTube 링크를 입력하세요:')
  if (!url) return

  if (!isYoutubeUrl(url)) {
    uiStore.showSnackbar({ text: '유효한 YouTube 링크가 아닙니다.', color: 'error' })
    return
  }

  editor.value
    .chain()
    .focus()
    .setYoutubeVideo({
      src: url,
    })
    .run()
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
    Youtube.configure({
      controls: true,
      nocookie: false,
      modestBranding: true,
    }),
  ],
  editorProps: {
    // 에디터 속성 추가 (이미지 처리)
    attributes: {
      class: 'prose prose-sm sm:prose-base max-w-none focus:outline-none',
    },
    handlePaste(view, event) {
      const items = event.clipboardData?.items
      if (items) {
        for (const item of items) {
          // 붙여넣기도 이미지 파일인지 확인
          if (item.type.startsWith('image/')) {
            event.preventDefault()
            const file = item.getAsFile()
            if (file) {
              handleImageUpload(file).then(insertImage)
            }
            return true
          }
        }
      }
      // YouTube URL 텍스트 붙여넣기 처리
      const text = event.clipboardData?.getData('text/plain')?.trim()
      if (text && isYoutubeUrl(text) && editor.value) {
        event.preventDefault()
        editor.value
          .chain()
          .focus()
          .setYoutubeVideo({
            src: text,
          })
          .run()
        return true
      }
      return false
    },
    handleDrop(view, event) {
      event.preventDefault()
      const files = event.dataTransfer?.files
      if (!files || files.length === 0) return

      const file = files[0]
      // 드래그 앤 드롭 시에도 이미지 파일이 아닌 경우 알림
      if (isImageFile(file)) {
        handleImageUpload(file).then(insertImage)
      } else {
        uiStore.showSnackbar({ text: '이미지 파일만 드롭하여 업로드할 수 있습니다.', color: 'error' })
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
  if (editor.value && editor.value.getHTML() !== newContent) {
    editor.value.commands.setContent(newContent, false)
  }
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

      <v-btn
        icon="mdi-image-plus"
        size="small"
        variant="text"
        @click="fileInput.click()"
      ></v-btn>
      <input
        type="file"
        ref="fileInput"
        @change="onFileChange"
        accept=".png, .jpg, .jpeg, .gif, .webp"
        hidden
      />
      <v-btn
        icon="mdi-youtube"
        size="small"
        variant="text"
        @click="insertYoutubeByPrompt"
      />
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

.editor-content :deep(iframe[src*="youtube.com"]) {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9; /* 16:9 비율 강제 */
  height: auto;
  border-radius: 8px;
  border: 0;
}
</style>
