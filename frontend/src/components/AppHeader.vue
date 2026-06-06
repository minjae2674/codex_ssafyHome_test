<template>
  <header class="app-header">
    <RouterLink class="brand" to="/">
      <span class="brand-mark">
        <Home :size="15" />
      </span>
      <span>SSAFY HOME</span>
    </RouterLink>

    <form v-if="isHomeRoute" class="header-search" @submit.prevent="submitHeaderSearch">
      <Search :size="15" />
      <input
        v-model.trim="headerKeyword"
        type="search"
        placeholder="아파트, 지역, 단지명 검색"
        @keydown.enter.prevent="submitHeaderSearch"
      />
    </form>
    <div v-else class="header-search-spacer"></div>

    <nav class="top-nav">
      <RouterLink to="/news">뉴스</RouterLink>
      <RouterLink to="/foreigners">외국인 히트맵</RouterLink>
      <RouterLink to="/myhome">마이홈</RouterLink>
      <RouterLink to="/favorites">관심</RouterLink>
    </nav>

    <div class="header-actions">
      <button class="ghost-button" type="button" title="언어 전환">
        <Globe :size="14" />
        <span>EN</span>
      </button>
      <button class="icon-only-button" type="button" title="알림">
        <Bell :size="16" />
      </button>
      <div v-if="authStore.isAuthenticated" class="user-menu">
        <span class="user-chip">
          <User :size="14" />
          {{ authStore.nickname }}
        </span>
        <button class="logout-button" type="button" @click="logout">
          로그아웃
        </button>
      </div>
      <RouterLink v-else class="login-link" to="/login">
        <User :size="14" />
        <span>로그인</span>
      </RouterLink>
      <RouterLink v-if="!authStore.isAuthenticated" class="primary-link" to="/signup">회원가입</RouterLink>
    </div>
  </header>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Bell, Globe, Home, Search, User } from 'lucide-vue-next';
import { useAuthStore } from '@/stores/authStore';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const headerKeyword = ref('');

const isHomeRoute = computed(() => route.name === 'home');

// 헤더 검색은 홈 화면의 route query로 전달한다.
// HomeView는 query.keyword를 감시하다가 아파트 검색 API를 다시 호출한다.
function submitHeaderSearch() {
  const keyword = headerKeyword.value || undefined;
  router.push({
    name: 'home',
    query: keyword ? { keyword } : {},
  });
}

function logout() {
  authStore.logout();
  router.push({ name: 'home' });
}

watch(
  () => route.query.keyword,
  (nextKeyword) => {
    headerKeyword.value = typeof nextKeyword === 'string' ? nextKeyword : '';
  },
  { immediate: true },
);
</script>
