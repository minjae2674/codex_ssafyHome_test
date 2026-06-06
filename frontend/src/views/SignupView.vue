<template>
  <main class="auth-page">
    <section class="auth-card">
      <div class="auth-heading">
        <p class="eyebrow">SSAFY HOME</p>
        <h1>회원가입</h1>
        <p>LOCAL 계정으로 먼저 시작하고, 이후 소셜 로그인은 확장할 수 있게 구성합니다.</p>
      </div>

      <form class="auth-form" @submit.prevent="submitSignup">
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
          <span>닉네임</span>
          <input
            v-model.trim="form.nickname"
            type="text"
            autocomplete="nickname"
            placeholder="입주민 커뮤니티에서 사용할 이름"
          />
        </label>

        <label>
          <span>비밀번호</span>
          <input
            v-model="form.password"
            type="password"
            autocomplete="new-password"
            placeholder="8자 이상"
          />
        </label>

        <label>
          <span>비밀번호 확인</span>
          <input
            v-model="form.passwordConfirm"
            type="password"
            autocomplete="new-password"
            placeholder="비밀번호를 한 번 더 입력"
          />
        </label>

        <p v-if="errorMessage" class="auth-error">{{ errorMessage }}</p>

        <button class="auth-submit" type="submit" :disabled="authStore.isLoading">
          {{ authStore.isLoading ? '가입 중...' : '회원가입' }}
        </button>
      </form>

      <p class="auth-switch">
        이미 계정이 있나요?
        <RouterLink to="/login">로그인</RouterLink>
      </p>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

const form = reactive({
  email: '',
  nickname: '',
  password: '',
  passwordConfirm: '',
});
const errorMessage = ref('');

async function submitSignup() {
  errorMessage.value = '';

  if (form.password !== form.passwordConfirm) {
    errorMessage.value = '비밀번호 확인이 일치하지 않습니다.';
    return;
  }

  try {
    await authStore.signup({
      email: form.email,
      nickname: form.nickname,
      password: form.password,
    });

    router.push('/');
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || '회원가입에 실패했습니다.';
  }
}
</script>
