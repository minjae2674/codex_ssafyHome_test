import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '@/views/HomeView.vue';
import PlaceholderView from '@/views/PlaceholderView.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/login', name: 'login', component: PlaceholderView, meta: { title: '로그인' } },
    { path: '/signup', name: 'signup', component: PlaceholderView, meta: { title: '회원가입' } },
    { path: '/favorites', name: 'favorites', component: PlaceholderView, meta: { title: '관심 아파트' } },
    { path: '/myhome', name: 'myhome', component: PlaceholderView, meta: { title: '우리집 인증' } },
    { path: '/community/:aptSeq', name: 'community', component: PlaceholderView, meta: { title: '입주민 커뮤니티' } },
    { path: '/foreigners', name: 'foreigners', component: PlaceholderView, meta: { title: '외국인 히트맵' } },
    { path: '/news', name: 'news', component: PlaceholderView, meta: { title: '부동산 뉴스' } },
  ],
});

export default router;
