<script setup>
import { onMounted, ref } from 'vue'
import { useCommentsStore } from '@/stores/comments'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils/formatDate'

// defineProps: 컴포넌트가 부모로부터 전달받는 props를 선언
// postId라는 prop을 반드시 받아야 함
// props.postId를 통해 해당 게시글의 ID를 사용할 수 있음
const props = defineProps({
  postId: {
    type: Number,
    required: true,
  },
})

const commentsStore = useCommentsStore()
const authStore = useAuthStore()

const newCommentContent = ref('') // 댓글 작성 폼과 연결될 변수
const editingCommentId = ref(null) // 현재 수정 중인 댓글 id 저장할 변수
const editedContent = ref('') // 수정 중인 댓글 내용 담을 변수

onMounted(() => {
  commentsStore.fetchComments(props.postId)
})

function submitComment() {
  if (!newCommentContent.value) {
    alert('댓글 내용을 입력해주세요.')
    return
  }
  commentsStore.createComment({
    postId: props.postId,
    content: newCommentContent.value,
  })
  newCommentContent.value = '' // 작성 폼 초기화
}

function removeComment(commentId) {
  if (confirm('정말로 댓글을 삭제하시겠습니까?')) {
    commentsStore.deleteComment(props.postId, commentId)
  }
}

// 수정 모드로 전환
function startEdit(comment) {
  editingCommentId.value = comment.id
  editedContent.value = comment.content
}

function exitEdit() {
  editingCommentId.value = null
  editedContent.value = ''
}

function submitUpdate(commentId) {
  commentsStore.updateComment(props.postId, commentId, { content: editedContent.value })
  exitEdit()
}
</script>

<template>
  <v-container>
    <v-divider class="my-8"></v-divider>
    <h3 class="text-h6 mb-4">댓글 ({{ commentsStore.comments.length }})</h3>

    <v-list lines="two">
      <v-list-item v-for="comment in commentsStore.comments" :key="comment.id" class="mb-2">

        <div v-if="editingCommentId === comment.id">
          <v-textarea v-model="editedContent" rows="2" no-resize hide-details></v-textarea>
          <div class="mt-2 text-right">
            <v-btn size="small" @click="exitEdit">취소</v-btn>
            <v-btn size="small" color="primary" @click="submitUpdate(comment.id)">저장</v-btn>
          </div>
        </div>

        <div v-else>
          <div v-if="comment.isDeleted">
            <v-list-item-subtitle class="text-grey">삭제된 댓글입니다.</v-list-item-subtitle>
          </div>
          <div v-else>
            <v-list-item-title class="font-weight-bold">{{ comment.authorUsername }}</v-list-item-title>
            <v-list-item-subtitle>{{ comment.content }}</v-list-item-subtitle>
            <div>
              <span class="text-caption text-grey">{{ formatDateTime(comment.createdAt) }}</span>
              <span v-if="comment.updatedAt && comment.updatedAt !== comment.createdAt" class="text-caption text-grey">
                (수정됨)
              </span>
            </div>
          </div>
        </div>

        <template v-slot:append>
          <div v-if="!comment.isDeleted && authStore.username === comment.authorUsername && editingCommentId !== comment.id">
            <v-btn icon="mdi-pencil" variant="text" size="small" @click="startEdit(comment)"></v-btn>
            <v-btn icon="mdi-delete" variant="text" size="small" @click="removeComment(comment.id)"></v-btn>
          </div>
        </template>
      </v-list-item>
    </v-list>

    <v-card v-if="authStore.isLoggedIn" class="mt-6">
      <v-card-text>
        <v-textarea
            v-model="newCommentContent"
            label="댓글을 입력하세요"
            rows="3"
            no-resize
            hide-details
        ></v-textarea>
        <v-btn color="primary" class="mt-4" @click="submitComment">댓글 작성</v-btn>
      </v-card-text>
    </v-card>
  </v-container>
</template>
