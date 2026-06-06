import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import LoginView from '@/views/LoginView.vue';
import PlaceholderView from '@/views/PlaceholderView.vue';
import SignupView from '@/views/SignupView.vue';
import { useAuthStore } from '@/stores/authStore';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/login', name: 'login', component: LoginView, meta: { title: '로그인', guestOnly: true } },
    { path: '/signup', name: 'signup', component: SignupView, meta: { title: '회원가입', guestOnly: true } },
    { path: '/favorites', name: 'favorites', component: PlaceholderView, meta: { title: '관심 아파트', requiresAuth: true } },
    { path: '/myhome', name: 'myhome', component: PlaceholderView, meta: { title: '우리집 인증', requiresAuth: true } },
    { path: '/community/:aptSeq', name: 'community', component: PlaceholderView, meta: { title: '입주민 커뮤니티', requiresAuth: true } },
    { path: '/foreigners', name: 'foreigners', component: PlaceholderView, meta: { title: '외국인 히트맵' } },
    { path: '/news', name: 'news', component: PlaceholderView, meta: { title: '부동산 뉴스' } },
  ],
});

router.beforeEach((to) => {
  const authStore = useAuthStore();

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    };
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'home' };
  }

  return true;
});

export default router;
