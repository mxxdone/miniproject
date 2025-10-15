<script setup>
import { computed, onMounted, ref } from 'vue'
import { usePostsStore } from '@/stores/posts'
import { useCategoriesStore } from '@/stores/categories'
import { useRoute, useRouter } from 'vue-router'
import { useUiStore } from '@/stores/ui'
import TiptapEditor from '@/views/TiptapEditor.vue' // ui 스토어 import


const route = useRoute()
const postsStore = usePostsStore()
const categoriesStore = useCategoriesStore()
const router = useRouter() // 라우터 객체 가져오기
const uiStore = useUiStore() // ui 스토어 사용

// form 제어를 위한 ref를 생성
const form = ref(null)
const title = ref('')
const content = ref('')
const selectedCategoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : null)

const flatCategories = computed(() => {
  const result = []
  categoriesStore.categories.forEach((parent) => {
    // 상위 카테고리는 선택X, disabled: true 추가
    result.push({ ...parent, disabled: true })
    if (parent.children) {
      parent.children.forEach((child) => {
        // 하위 카테고리는 들여쓰기 된 것처럼 보이게
        result.push({ ...child, name: `\u00A0 ${child.name}` })
      })
    }
  })
  return result
})

// 페이지 로드시 카테고리 목록 불러오기
onMounted(async () => {
  categoriesStore.fetchCategories()
})

function goBack() {
  router.back()
}

// 유효성 검사 규칙들을 정의합니다.
const rules = {
  required: (value) => !!value || '필수 항목입니다.', // 값이 있으면 true, 없으면 메시지 반환
  minLength: (value) => value.length >= 2 || '2글자 이상 입력해 주세요.',
}

// submit 메서드를 비동기(async)로 변경하고, 폼의 유효성을 검사하는 로직 추가
async function submitPost() {
  const { valid } = await form.value.validate() // 폼의 유효성을 검사하고 결과를 받음
  // 에디터 내용이 비어있는지 검사
  if (!content.value || content.value === '<p></p>') {
    uiStore.showSnackbar({ text: '내용을 입력해주세요.', color: 'error' })
    return // 내용이 없으면 여기서 함수를 중단합니다.
  }

  // 모든 검사를 통과했을 때만 서버에 전송합니다.
  if (valid) {
    const isSuccess = await postsStore.createPost({
      title: title.value,
      content: content.value,
      categoryId: selectedCategoryId.value,
    })

    if (isSuccess) {
      uiStore.showSnackbar({ text: '게시글이 성공적으로 작성되었습니다.', color: 'success' })
      await router.push(`/?category=${selectedCategoryId.value}`)
    } else {
      uiStore.showSnackbar({ text: '게시글 생성에 실패했습니다.', color: 'error' })
    }
  }
}
</script>

<template>
  <v-container>
    <v-card>
      <v-card-title class="text-h5">새 게시글 작성</v-card-title>
      <v-card-text>
        <v-form ref="form" @submit.prevent="submitPost">
          <v-select
            v-model="selectedCategoryId"
            :items="flatCategories"
            item-title="name"
            item-value="id"
            :item-props="(item) => ({ disabled: item.disabled })"
            label="카테고리"
            :rules="[rules.required]"
            required
          ></v-select>
          <v-text-field
            v-model="title"
            label="제목"
            :rules="[rules.required, rules.minLength]"
            required
          ></v-text-field>
          <div class="text-subtitle-1 font-weight-medium mb-2">내용</div>
          <!-- Tiptap 에디터 추가 -->
          <TiptapEditor v-model:content="content" />
          <v-btn color="primary" @click="goBack" class="mt-4 mr-2">뒤로가기</v-btn>
          <v-btn type="submit" color="primary" class="mt-4">등록</v-btn>
        </v-form>
      </v-card-text>
    </v-card>
  </v-container>
</template>
