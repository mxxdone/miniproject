<script setup>
import { onMounted, computed } from 'vue'
import { useNotificationsStore } from '@/stores/notifications.js'
import { useRouter } from 'vue-router'
import { formatDateTime } from '@/utils/formatDate'
import { useTheme } from 'vuetify'

const store = useNotificationsStore()
const router = useRouter()
const theme = useTheme()

const activeColor = computed(() => {
  return theme.global.current.value.dark ? 'secondary' : 'primary'
})

// 읽지 않은 알림만 걸러내기
const unreadNotifications = computed(() => {
  const list = store.notifications || []
  return list.filter(n => !n.isRead)
})

onMounted(() => {
  store.fetchUnreadCount()
})

function onMenuOpen() {
  store.fetchNotifications()
  store.fetchUnreadCount()
}

async function onClickNotification(notification) {
  if (!notification.isRead) {
    await store.markAsRead(notification.id)
  }
  router.push(notification.url)
}

async function onMarkAllAsRead() {
  await store.markAllAsRead()
}
</script>

<template>
  <v-menu location="bottom end" :close-on-content-click="true">
    <template v-slot:activator="{ props }">
      <v-btn icon color="white" v-bind="props" @click="onMenuOpen" class="mr-2">
        <v-badge
          :content="store.unreadCount"
          :model-value="store.unreadCount > 0"
          color="error"
          overlap
        >
          <v-icon>mdi-bell-outline</v-icon>
        </v-badge>
      </v-btn>
    </template>

    <v-card width="400" max-height="500" class="overflow-y-auto" border>
      <v-card-title
        class="d-flex justify-space-between align-center py-3 px-4 text-subtitle-1 font-weight-bold"
      >
        <span>알림</span>
        <v-btn
          v-if="unreadNotifications.length > 0"
          variant="text"
          size="small"
          :color="activeColor"
          class="px-0"
          @click.stop="onMarkAllAsRead"
        >
          모두 읽음
        </v-btn>
      </v-card-title>

      <v-divider></v-divider>

      <v-list v-if="unreadNotifications.length > 0" lines="three" class="py-0">
        <template v-for="(item, index) in unreadNotifications" :key="item.id">
          <v-list-item @click="onClickNotification(item)" link>
            <template v-slot:prepend>
              <v-icon :color="item.type === 'LIKE' ? 'pink' : activeColor">
                {{ item.type === 'LIKE' ? 'mdi-heart' : 'mdi-comment-text' }}
              </v-icon>
            </template>

            <v-list-item-title class="font-weight-bold">
              {{ item.content }}
            </v-list-item-title>

            <v-list-item-subtitle class="text-caption mt-1">
              {{ formatDateTime(item.createdAt) }}
            </v-list-item-subtitle>

            <template v-slot:append>
              <v-icon :color="activeColor" size="x-small">mdi-circle</v-icon>
            </template>
          </v-list-item>
          <v-divider v-if="index < unreadNotifications.length - 1"></v-divider>
        </template>
      </v-list>

      <div
        v-else
        class="d-flex flex-column justify-center align-center text-grey"
        style="min-height: 200px;"
      >
        <v-icon size="x-large" class="mb-3">mdi-bell-sleep</v-icon>
        <div>새로운 알림이 없습니다.</div>
      </div>
    </v-card>
  </v-menu>
</template>
