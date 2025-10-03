import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from "@/stores/auth"

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/posts/new', // 새 글 작성 경로 추가
      name: 'postCreate',
      // () =>을 사용하여 지연로딩, 필요할 때 import 해준다
      // view 컴포넌트를 연결
      component: () => import('../views/PostCreateView.vue')
    },
    {
      path: '/posts/:id', // :id는 동적으로 변하는 값을 의미
      name: 'postDetail',
      component: () => import('../views/PostDetailView.vue')
    },
    {
      path: '/posts/:id/edit', // 수정 페이지 경로
      name: 'postEdit',
      component: () => import('../views/PostEditView.vue')
      //
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/SignUpView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/about',
      name: 'aboutMe',
      component: () => import('../views/AboutMeView.vue')
    },
    {
      path: '/oauth2/redirect',
      name: 'oauth2Redirect',
      component: () => import('../views/OAuth2RedirectHandler.vue')
    }
  ]
})

// 네비게이션 가드: 권한 없이 주소로 접속시 로그인 페이지로
router.beforeEach((to) => {
  // 스토어는 가드 함수 내부에서 호출
  const authStore = useAuthStore()

  // 로그인이 필요한 페이지 목록
  const protectedRoutes = ['postCreate', 'postEdit']
  // 로그인하지 않은 사용자만 접근해야 하는 페이지 목록
  const publicOnlyRoutes = ['login', 'signup']

  const isLoggedIn = authStore.isLoggedIn


  // 이동하려는 페이지가 보호된 페이지 & 비로그인 상태라면
  if (protectedRoutes.includes(to.name) && !isLoggedIn) {
    alert('로그인이 필요한 페이지입니다.')
    return {
      name :  'login',
      query: { redirect: to.fullPath } // 사용자가 이용하려던 경로를 보내줌
    }
  }

  // 로그인/회원가입 페이지에 접근하려는데 이미 로그인 된 경우
  if (publicOnlyRoutes.includes(to.name) && isLoggedIn) {
    // 메인 페이지로 리디렉션
    return { name: 'home' }
  }
})

export default router
