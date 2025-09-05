<script setup>
import { onMounted, ref, computed } from 'vue'
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

const newCommentContent = ref('') // 댓글 작성 폼과 연결될 변수, 상위 댓글
const editingCommentId = ref(null) // 현재 수정 중인 댓글 id 저장할 변수
const editedContent = ref('') // 수정 중인 댓글 내용 담을 변수
const newReplyContent = ref('')   // 대댓글 내용
const replyingToCommentId = ref(null) // 대댓글을 작성 중인 상위 댓글 ID

const commentCount = computed(() => {
  let count = 0
  for (const c of commentsStore.comments) {
    count += 1 // 자기 자신
    count += c.children ? c.children.length : 0 // 대댓글
  }
  return count
})

onMounted(() => {
  commentsStore.fetchComments(props.postId)
})

// 최상위 댓글 작성
function submitComment() {
  if (!newCommentContent.value) {
    alert('내용을 입력해주세요.')
    return
  }
  commentsStore.createComment({
    postId: props.postId,
    content: newCommentContent.value,
    parentId: null // 최상위 댓글이므로 parentId는 null
  })
  newCommentContent.value = ''
}

// 대댓글 작성
function submitReply(parentId){
  if (!newReplyContent.value) {
    alert('내용을 입력해주세요.')
    return
  }
  commentsStore.createComment({
    postId: props.postId,
    content: newReplyContent.value,
    parentId: parentId
  })
  newReplyContent.value = ''
  replyingToCommentId.value = null // 대댓글 폼 닫기
}

// 대댓글 폼 열고 닫기
function openReplyForm(commentId) {
  replyingToCommentId.value = replyingToCommentId.value === commentId ? null : commentId;
  newReplyContent.value = ''
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
    <h3 class="text-h6 mb-4">
      댓글<span class="text-black">({{ commentCount }})</span>
    </h3>

    <v-list lines="two" class="bg-transparent">
      <template v-for="(comment, index) in commentsStore.comments" :key="comment.id">
        <v-list-item class="mb-2 px-0">
          <div v-if="editingCommentId === comment.id">
            <v-textarea v-model="editedContent" rows="2" no-resize hide-details></v-textarea>
            <div class="mt-2 text-right">
              <v-btn size="small" @click="exitEdit">닫기</v-btn>
              <v-btn size="small" color="primary" @click="submitUpdate(comment.id)">등록</v-btn>
            </div>
          </div>
          <div v-else>
            <div v-if="comment.isDeleted">
              <v-list-item-subtitle class="text-grey">삭제된 댓글입니다.</v-list-item-subtitle>
            </div>
            <div v-else>
              <v-list-item-title class="font-weight-bold">{{ comment.authorUsername }}</v-list-item-title>
              <v-list-item-subtitle style="white-space: pre-wrap;">{{ comment.content }}</v-list-item-subtitle>
              <div>
                <span class="text-caption text-grey">{{ formatDateTime(comment.createdAt) }}</span>
                <span v-if="comment.updatedAt && comment.updatedAt !== comment.createdAt" class="text-caption text-grey">(수정됨)</span>
              </div>
            </div>
          </div>
          <template v-slot:append>
            <div v-if="!comment.isDeleted && authStore.isLoggedIn && editingCommentId !== comment.id" class="d-flex align-center">
              <v-btn icon="mdi-comment-outline" variant="text" size="small" @click="openReplyForm(comment.id)"></v-btn>
              <div v-if="authStore.username === comment.authorUsername">
                <v-btn icon="mdi-pencil" variant="text" size="small" @click="startEdit(comment)"></v-btn>
                <v-btn icon="mdi-delete" variant="text" size="small" @click="removeComment(comment.id)"></v-btn>
              </div>
            </div>
          </template>
        </v-list-item>

        <div class="pl-10">
          <v-list-item v-for="reply in comment.children" :key="reply.id" class="mb-2">

            <template v-slot:prepend>
              <v-icon>mdi-subdirectory-arrow-right</v-icon>
            </template>

            <div v-if="editingCommentId === reply.id">
              <v-textarea v-model="editedContent" rows="2" no-resize hide-details></v-textarea>
              <div class="mt-2 text-right">
                <v-btn size="small" @click="exitEdit">닫기</v-btn>
                <v-btn size="small" color="primary" @click="submitUpdate(reply.id)">등록</v-btn>
              </div>
            </div>

            <div v-else>
              <v-list-item-title class="font-weight-bold">{{ reply.authorUsername }}</v-list-item-title>
              <v-list-item-subtitle style="white-space: pre-wrap;">{{ reply.content }}</v-list-item-subtitle>
              <div>
                <span class="text-caption text-grey">{{ formatDateTime(reply.createdAt) }}</span>
                <span v-if="reply.updatedAt && reply.updatedAt !== reply.createdAt" class="text-caption text-grey">(수정됨)</span>
              </div>
            </div>

            <template v-slot:append>
              <div v-if="!reply.isDeleted && authStore.username === reply.authorUsername && editingCommentId !== reply.id">
                <v-btn icon="mdi-pencil" variant="text" size="small" @click="startEdit(reply)"></v-btn>
                <v-btn icon="mdi-delete" variant="text" size="small" @click="removeComment(reply.id)"></v-btn>
              </div>
            </template>
          </v-list-item>
        </div>

        <v-card v-if="replyingToCommentId === comment.id" class="pl-10 mb-4" flat>
          <v-textarea v-model="newReplyContent" label="대댓글을 입력하세요" rows="2" no-resize hide-details></v-textarea>
          <div class="text-right mt-2">
            <v-btn size="small" @click="replyingToCommentId = null">닫기</v-btn>
            <v-btn size="small" color="primary" @click="submitReply(comment.id)">등록</v-btn>
          </div>
        </v-card>

        <v-divider v-if="index < commentsStore.comments.length - 1" class="my-2"></v-divider>
      </template>
    </v-list>

    <v-card v-if="authStore.isLoggedIn && !editingCommentId && !replyingToCommentId" class="mt-6">
      <v-card-text>
        <v-textarea v-model="newCommentContent" label="댓글을 입력하세요" rows="3" no-resize hide-details></v-textarea>
        <div class="text-right mt-4">
          <v-btn color="primary" @click="submitComment">등록</v-btn>
        </div>
      </v-card-text>
    </v-card>
  </v-container>
</template>
