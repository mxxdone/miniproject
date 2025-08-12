import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

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
    }
  ]
})

export default router
