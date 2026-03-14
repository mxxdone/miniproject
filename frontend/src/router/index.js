import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/:parentSlug/:childSlug?/posts/new',
      name: 'postCreate',
      // () =>을 사용하여 지연로딩, 필요할 때 import 해준다
      // view 컴포넌트를 연결
      component: () => import('../views/PostCreateView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/SignUpView.vue'),
      meta: { publicOnly: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { publicOnly: true },
    },
    {
      path: '/mypage',
      name: 'myPage',
      component: () => import('../views/MyPageView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/about-me',
      name: 'aboutMe',
      component: () => import('../views/AboutMeView.vue'),
    },
    {
      path: '/oauth2/redirect',
      name: 'oauth2Redirect',
      component: () => import('../views/OAuth2RedirectHandler.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      redirect: '/', // 존재하지 않는 경로로 접근 시 홈으로 리다이렉트
    },
    // 상위 카테고리 경로 (e.g., /project)
    {
      path: '/:parentSlug',
      name: 'parentCategory',
      component: HomeView,
    },
    // 하위 카테고리 경로 (e.g., /backend/java-spring)
    {
      path: '/:parentSlug/:childSlug',
      name: 'childCategory',
      component: HomeView,
    },
    {
      path: '/:parentSlug/:childSlug/posts/:id', // :id는 동적으로 변하는 값을 의미
      name: 'postDetail',
      component: () => import('../views/PostDetailView.vue'),
    },
    {
      path: '/:parentSlug/:childSlug/posts/:id/edit',
      name: 'postEdit',
      component: () => import('../views/PostEditView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/admin',
      name: 'AdminDashboard',
      component: () => import('../views/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, role: 'ADMIN' },
    },
  ],
})

// 네비게이션 가드: 권한 없이 주소로 접속시 로그인 페이지로
router.beforeEach((to) => {
  // 스토어는 가드 함수 내부에서 호출
  const authStore = useAuthStore()
  const isLoggedIn = authStore.isLoggedIn

  // 이동하려는 페이지가 보호된 페이지 & 비로그인 상태라면
  if (to.meta.requiresAuth && !isLoggedIn) {
    alert('로그인이 필요한 페이지입니다.')
    return {
      name: 'login',
      query: { redirect: to.fullPath }, // 사용자가 이용하려던 경로를 보내줌
    }
  }

  // ADMIN 권한이 필요한 페이지
  if (to.meta.role === 'ADMIN' && !authStore.isAdmin) {
    alert('접근 권한이 없습니다.')
    return { name: 'home' }
  }

  // 로그인 후 로그인/회원가입 페이지에 접근
  if (to.meta.publicOnly && isLoggedIn) {
    // 메인 페이지로 리디렉션
    return { name: 'home' }
  }
})

export default router
