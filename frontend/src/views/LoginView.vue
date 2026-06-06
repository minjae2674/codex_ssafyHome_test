<template>
  <main class="auth-page">
    <section class="auth-card">
      <div class="auth-heading">
        <p class="eyebrow">SSAFY HOME</p>
        <h1>로그인</h1>
        <p>관심 아파트, 마이홈, 입주민 커뮤니티를 이용하려면 로그인해주세요.</p>
      </div>

      <form class="auth-form" @submit.prevent="submitLogin">
        <label>
          <span>이메일</span>
          <input
            v-model.trim="form.email"
            type="email"
            autocomplete="email"
            placeholder="ssafy@example.com"
          />
        </label>

        <label>
          <span>비밀번호</span>
          <input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            placeholder="8자 이상"
          />
        </label>

        <p v-if="errorMessage" class="auth-error">{{ errorMessage }}</p>

        <button class="auth-submit" type="submit" :disabled="authStore.isLoading">
          {{ authStore.isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <p class="auth-switch">
        아직 계정이 없나요?
        <RouterLink to="/signup">회원가입</RouterLink>
      </p>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const form = reactive({
  email: '',
  password: '',
});
const errorMessage = ref('');

async function submitLogin() {
  errorMessage.value = '';

  try {
    await authStore.login({
      email: form.email,
      password: form.password,
    });

    const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : '/';
    router.push(redirectPath);
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || '로그인에 실패했습니다.';
  }
}
</script>
