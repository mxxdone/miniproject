<script setup>
import { computed, onMounted, ref } from 'vue'
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
const newReplyContent = ref('') // 대댓글 내용
const replyingToCommentId = ref(null) // 대댓글을 작성 중인 상위 댓글 ID
const guestName = ref('')
const guestPassword = ref('')
const replyingGuestName = ref('')
const replyingGuestPassword = ref('')
const isPasswordDialogOpen = ref(false) // 다이얼로그 열림/닫힘 상태
const passwordToDelete = ref('')      // 다이얼로그에 입력된 비밀번호
const commentIdToDelete = ref(null)   // 삭제할 댓글의 ID


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

  const payload = {
    postId: props.postId,
    content: newCommentContent.value,
    parentId: null, // 최상위 댓글이므로 parentId는 null
  }

  if (!authStore.isLoggedIn) {
    if (!guestName.value || !guestPassword.value) {
      alert('닉네임과 비밀번호를 모두 입력해주세요.')
      return
    }
    payload.guestName = guestName.value
    payload.guestPassword = guestPassword.value
  }
  commentsStore.createComment(payload)

  newCommentContent.value = ''
  guestName.value = ''
  guestPassword.value = ''
}

// 대댓글 작성
function submitReply(parentId) {
  if (!newReplyContent.value) {
    alert('내용을 입력해주세요.')
    return
  }
  const payload = {
    postId: props.postId,
    content: newReplyContent.value,
    parentId: parentId,
  }
  if (!authStore.isLoggedIn) {
    if (!replyingGuestName.value || !replyingGuestPassword.value) {
      alert('닉네임과 비밀번호를 모두 입력해주세요.')
      return
    }
    payload.guestName = replyingGuestName.value
    payload.guestPassword = replyingGuestPassword.value
  }
  commentsStore.createComment(payload)
  newReplyContent.value = ''
  replyingGuestName.value = ''
  replyingGuestPassword.value = ''
  replyingToCommentId.value = null
}

// 대댓글 폼 열고 닫기
function openReplyForm(commentId) {
  replyingToCommentId.value = replyingToCommentId.value === commentId ? null : commentId
  newReplyContent.value = ''
}

async function removeComment(comment) {
  // 관리자 댓글 삭제
  if (authStore.isAdmin) {
    if (confirm('정말로 댓글을 삭제하시겠습니까?')) {
      commentsStore.deleteComment(props.postId, comment.id, null)
    }
    return
  }
  // 게스트 댓글 삭제: 다이얼로그 오픈
  if (comment.isGuest) {
    commentIdToDelete.value = comment.id
    isPasswordDialogOpen.value = true
  } else {
    // 회원일 경우 기존처럼 바로 confirm 창을 띄움
    if (confirm('정말로 댓글을 삭제하시겠습니까?')) {
      commentsStore.deleteComment(props.postId, comment.id, null)
    }
  }
}
// 게스트 댓글 삭제시 확인
async function confirmDeleteWithPassword() {
  if (!passwordToDelete.value) {
    alert('비밀번호를 입력해주세요.')
    return
  }
  // 삭제 액션 호출 후 다이얼로그 닫기
  await commentsStore.deleteComment(props.postId, commentIdToDelete.value, passwordToDelete.value)
  isPasswordDialogOpen.value = false
  passwordToDelete.value = ''
  commentIdToDelete.value = null
}

// 수정 모드로 전환
function startEdit(comment) {
  if (authStore.isAdmin || (!comment.isGuest && authStore.username === comment.authorUsername)) {
    editingCommentId.value = comment.id
    editedContent.value = comment.content
  } else {
    alert('댓글 수정 권한이 없습니다.');
  }
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
    <!-- 게스트 댓글 삭제 확인 다이얼로그 -->
    <v-dialog v-model="isPasswordDialogOpen" max-width="400">
      <v-card>
        <v-card-title>비밀번호 확인</v-card-title>
        <v-card-text>
          비밀번호를 입력하세요.
          <v-text-field
            v-model="passwordToDelete"
            type="password"
            autofocus
            @keyup.enter="confirmDeleteWithPassword"
          ></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="isPasswordDialogOpen = false">취소</v-btn>
          <v-btn color="primary" @click="confirmDeleteWithPassword">삭제</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

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
              <v-list-item-title class="font-weight-bold">{{
                comment.authorUsername
              }}</v-list-item-title>
              <v-list-item-subtitle class="text-grey">삭제된 댓글입니다.</v-list-item-subtitle>
            </div>
            <div v-else>
              <v-list-item-title class="font-weight-bold">{{
                comment.authorUsername
              }}</v-list-item-title>
              <v-list-item-subtitle style="white-space: pre-wrap">{{
                comment.content
              }}</v-list-item-subtitle>
              <div>
                <span class="text-caption text-grey">{{ formatDateTime(comment.createdAt) }}</span>
                <span
                  v-if="comment.updatedAt && comment.updatedAt !== comment.createdAt"
                  class="text-caption text-grey"
                  >(수정됨)</span
                >
              </div>
            </div>
          </div>
          <template v-slot:append>
            <div
              v-if="!comment.isDeleted && editingCommentId !== comment.id"
              class="d-flex align-center"
            >
              <v-btn
                icon="mdi-comment-outline"
                variant="text"
                size="small"
                @click="openReplyForm(comment.id)"
              ></v-btn>

              <div
                v-if="
                  authStore.isLoggedIn &&
                  (authStore.isAdmin || authStore.username === comment.authorUsername)
                "
              >
                <v-btn
                  v-if="!comment.isGuest"
                  icon="mdi-pencil"
                  variant="text"
                  size="small"
                  @click="startEdit(comment)"
                ></v-btn>
                <v-btn
                  icon="mdi-delete"
                  variant="text"
                  size="small"
                  @click="removeComment(comment)"
                ></v-btn>
              </div>

              <div v-if="!authStore.isLoggedIn && comment.isGuest">
                <v-btn
                  icon="mdi-delete"
                  variant="text"
                  size="small"
                  @click="removeComment(comment)"
                ></v-btn>
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

            <div v-else-if="reply.isDeleted">
              <v-list-item-title class="font-weight-bold">{{
                reply.authorUsername
              }}</v-list-item-title>
              <v-list-item-subtitle class="text-grey">삭제된 댓글입니다.</v-list-item-subtitle>
            </div>

            <div v-else>
              <v-list-item-title class="font-weight-bold">{{
                reply.authorUsername
              }}</v-list-item-title>
              <v-list-item-subtitle style="white-space: pre-wrap">{{
                reply.content
              }}</v-list-item-subtitle>
              <div>
                <span class="text-caption text-grey">{{ formatDateTime(reply.createdAt) }}</span>
                <span
                  v-if="reply.updatedAt && reply.updatedAt !== reply.createdAt"
                  class="text-caption text-grey"
                  >(수정됨)</span
                >
              </div>
            </div>

            <template v-slot:append>
              <div
                v-if="!reply.isDeleted && editingCommentId !== reply.id"
                class="d-flex align-center"
              >
                <div
                  v-if="
                    authStore.isLoggedIn &&
                    (authStore.isAdmin || authStore.username === reply.authorUsername)
                  "
                >
                  <v-btn
                    v-if="!reply.isGuest"
                    icon="mdi-pencil"
                    variant="text"
                    size="small"
                    @click="startEdit(reply)"
                  ></v-btn>
                  <v-btn
                    icon="mdi-delete"
                    variant="text"
                    size="small"
                    @click="removeComment(reply)"
                  ></v-btn>
                </div>
                <div v-if="!authStore.isLoggedIn && reply.isGuest">
                  <v-btn
                    icon="mdi-delete"
                    variant="text"
                    size="small"
                    @click="removeComment(reply)"
                  ></v-btn>
                </div>
              </div>
            </template>
          </v-list-item>
        </div>

        <v-card
          v-if="replyingToCommentId === comment.id && !editingCommentId"
          class="pl-10 mb-4"
          flat
        >
          <div v-if="authStore.isLoggedIn">
            <v-textarea
              v-model="newReplyContent"
              label="대댓글을 입력하세요"
              rows="2"
              no-resize
              hide-details
            ></v-textarea>
            <div class="text-right mt-2">
              <v-btn size="small" @click="replyingToCommentId = null">닫기</v-btn>
              <v-btn size="small" color="primary" @click="submitReply(comment.id)">등록</v-btn>
            </div>
          </div>
          <div v-else>
            <v-row>
              <v-col cols="6"
                ><v-text-field
                  v-model="replyingGuestName"
                  label="닉네임"
                  density="compact"
                  hide-details
                ></v-text-field
              ></v-col>
              <v-col cols="6"
                ><v-text-field
                  v-model="replyingGuestPassword"
                  label="비밀번호"
                  type="password"
                  density="compact"
                  hide-details
                ></v-text-field
              ></v-col>
              <v-textarea
                v-model="newReplyContent"
                label="대댓글을 입력하세요"
                rows="2"
                no-resize
                hide-details
                class="mt-4"
              ></v-textarea>
              <div class="text-right mt-2">
                <v-btn size="small" @click="replyingToCommentId = null">닫기</v-btn>
                <v-btn size="small" color="primary" @click="submitReply(comment.id)">등록</v-btn>
              </div>
            </v-row>
          </div>
        </v-card>

        <v-divider v-if="index < commentsStore.comments.length - 1" class="my-2"></v-divider>
      </template>
    </v-list>

    <v-card v-if="authStore.isLoggedIn && !editingCommentId && !replyingToCommentId" class="mt-6">
      <v-card-text>
        <v-textarea
          v-model="newCommentContent"
          label="댓글을 입력하세요"
          rows="3"
          no-resize
          hide-details
        ></v-textarea>
        <div class="text-right mt-4">
          <v-btn color="primary" @click="submitComment">등록</v-btn>
        </div>
      </v-card-text>
    </v-card>
    <v-card
      v-else-if="!authStore.isLoggedIn && !editingCommentId && !replyingToCommentId"
      class="mt-6"
    >
      <v-card-text>
        <v-row>
          <v-col cols="6">
            <v-text-field
              v-model="guestName"
              label="닉네임"
              density="compact"
              hide-details
            ></v-text-field>
          </v-col>
          <v-col cols="6">
            <v-text-field
              v-model="guestPassword"
              label="비밀번호"
              type="password"
              density="compact"
              hide-details
            ></v-text-field>
          </v-col>
        </v-row>
        <v-textarea
          v-model="newCommentContent"
          label="댓글을 입력하세요"
          rows="3"
          no-resize
          hide-details
          class="mt-4"
        ></v-textarea>
        <div class="text-right mt-4">
          <v-btn color="primary" @click="submitComment">등록</v-btn>
        </div>
      </v-card-text>
    </v-card>
  </v-container>
</template>
