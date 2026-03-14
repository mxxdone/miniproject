<!-- src/views/admin/AdminDashboard.vue -->
<template>
  <v-container class="py-6" style="max-width: 1200px">
    <!-- 헤더 -->
    <div class="d-flex align-center mb-6">
      <v-icon class="mr-2" size="28">mdi-view-dashboard</v-icon>
      <h1 class="text-h5 font-weight-bold">관리자 대시보드</h1>
      <v-spacer />
      <v-btn
        variant="outlined"
        size="small"
        :loading="store.loading"
        @click="store.fetchDashboard()"
      >
        <v-icon start>mdi-refresh</v-icon>
        새로고침
      </v-btn>
    </div>

    <!-- 로딩 -->
    <div v-if="store.loading && !store.summary" class="d-flex justify-center py-16">
      <v-progress-circular indeterminate color="primary" size="48" />
    </div>

    <!-- 에러 -->
    <v-alert v-else-if="store.error" type="error" variant="tonal" class="mb-6">
      {{ store.error }}
    </v-alert>

    <template v-else-if="store.summary">
      <!-- 요약 카드 -->
      <v-row class="mb-6">
        <v-col v-for="card in summaryCards" :key="card.label" cols="12" sm="6" md="4" lg="2">
          <v-card variant="outlined" class="text-center pa-4" rounded="lg">
            <v-icon :color="card.color" size="28" class="mb-2">{{ card.icon }}</v-icon>
            <div class="text-h5 font-weight-bold">{{ card.value }}</div>
            <div class="text-caption text-medium-emphasis">{{ card.label }}</div>
          </v-card>
        </v-col>
      </v-row>

      <!-- 일별 댓글 추이 차트 -->
      <v-card variant="outlined" rounded="lg" class="mb-6">
        <v-card-title class="text-subtitle-1 font-weight-bold">
          <v-icon start size="20">mdi-chart-line</v-icon>
          일별 댓글 추이 (최근 30일)
        </v-card-title>
        <v-card-text>
          <div
            v-if="store.commentsDailyStats.length === 0"
            class="text-center text-medium-emphasis py-8"
          >
            데이터가 없습니다.
          </div>
          <Line v-else :data="chartData" :options="chartOptions" style="max-height: 300px" />
        </v-card-text>
      </v-card>

      <v-row>
        <!-- 인기 게시글 -->
        <v-col cols="12" md="6">
          <v-card variant="outlined" rounded="lg" class="fill-height">
            <v-card-title class="text-subtitle-1 font-weight-bold">
              <v-icon start size="20">mdi-fire</v-icon>
              인기 게시글 TOP 5
            </v-card-title>
            <v-card-text class="pa-0">
              <v-list v-if="store.popularPosts.length > 0" lines="two" density="comfortable">
                <v-list-item
                  v-for="(post, index) in store.popularPosts"
                  :key="post.id"
                  :to="`/${post.parentSlug}/${post.childSlug}/posts/${post.id}`"
                >
                  <template #prepend>
                    <v-avatar
                      :color="index < 3 ? 'primary' : 'grey-lighten-1'"
                      size="28"
                      class="mr-3"
                    >
                      <span class="text-caption font-weight-bold">{{ index + 1 }}</span>
                    </v-avatar>
                  </template>
                  <v-list-item-title class="text-body-2 font-weight-medium">
                    {{ post.title }}
                  </v-list-item-title>
                  <v-list-item-subtitle class="text-caption">
                    <v-chip size="x-small" variant="tonal" class="mr-1" v-if="post.categoryName">
                      {{ post.categoryName }}
                    </v-chip>
                    <v-icon size="12" class="mr-1">mdi-eye</v-icon>{{ post.viewCount }}
                    <v-icon size="12" class="ml-2 mr-1">mdi-heart</v-icon>{{ post.likeCount }}
                    <v-icon size="12" class="ml-2 mr-1">mdi-comment</v-icon>{{ post.commentCount }}
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
              <div v-else class="text-center text-medium-emphasis py-8">게시글이 없습니다.</div>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- 최근 댓글 -->
        <v-col cols="12" md="6">
          <v-card variant="outlined" rounded="lg" class="fill-height">
            <v-card-title class="text-subtitle-1 font-weight-bold">
              <v-icon start size="20">mdi-comment-text-multiple</v-icon>
              최근 댓글
            </v-card-title>
            <v-card-text class="pa-0">
              <v-list v-if="store.recentComments.length > 0" lines="three" density="comfortable">
                <v-list-item
                  v-for="comment in store.recentComments"
                  :key="comment.id"
                  :to="`/${comment.parentSlug}/${comment.childSlug}/posts/${comment.postId}`"
                >
                  <v-list-item-title class="text-body-2">
                    {{ truncate(comment.content, 50) }}
                  </v-list-item-title>
                  <v-list-item-subtitle class="text-caption">
                    <span class="font-weight-medium">{{ comment.authorNickname }}</span>
                    · {{ comment.postTitle }}
                  </v-list-item-subtitle>
                  <v-list-item-subtitle class="text-caption text-medium-emphasis">
                    {{ formatDate(comment.createdAt) }}
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
              <div v-else class="text-center text-medium-emphasis py-8">댓글이 없습니다.</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAdminStore } from '@/stores/admin.js'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Filler,
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Filler)

const store = useAdminStore()

onMounted(() => {
  store.fetchDashboard()
})

// summary
const summaryCards = computed(() => {
  const s = store.summary
  if (!s) return []
  return [
    { label: '총 게시글', value: s.totalPosts, icon: 'mdi-file-document', color: 'blue' },
    { label: '총 회원', value: s.totalUsers, icon: 'mdi-account-group', color: 'green' },
    { label: '총 댓글', value: s.totalComments, icon: 'mdi-comment-multiple', color: 'orange' },
    { label: '오늘 신규 가입', value: s.todayNewUsers, icon: 'mdi-account-plus', color: 'teal' },
    { label: '오늘 게시글', value: s.todayPosts, icon: 'mdi-pencil-plus', color: 'purple' },
    { label: '오늘 댓글', value: s.todayComments, icon: 'mdi-comment-plus', color: 'red' },
  ]
})

// 차트
const chartData = computed(() => ({
  labels: store.commentsDailyStats.map((item) => {
    const date = new Date(item.date)
    return `${date.getMonth() + 1}/${date.getDate()}`
  }),
  datasets: [
    {
      label: '댓글 수',
      data: store.commentsDailyStats.map((item) => item.count),
      borderColor: '#1867C0',
      backgroundColor: 'rgba(24, 103, 192, 0.1)',
      fill: true,
      tension: 0.3,
      pointRadius: 3,
      pointHoverRadius: 6,
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    tooltip: {
      callbacks: {
        title: (items) => {
          const idx = items[0].dataIndex
          return store.commentsDailyStats[idx]?.date || ''
        },
      },
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: { stepSize: 1 },
    },
  },
}

// 유틸
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}.${m}.${d} ${h}:${min}`
}

function truncate(text, length) {
  if (!text) return ''
  // HTML 태그 제거 후 자르기
  const stripped = text.replace(/<[^>]*>/g, '')
  return stripped.length > length ? stripped.substring(0, length) + '...' : stripped
}
</script>
