<template>
  <main class="placeholder-page">
    <section class="placeholder-panel">
      <p class="eyebrow">SSAFY HOME</p>
      <h1>{{ route.meta.title }}</h1>
      <p>다음 단계에서 연결할 화면입니다.</p>
    </section>
  </main>
</template>

<script setup>
import { useRoute } from 'vue-router';

const route = useRoute();
</script>
