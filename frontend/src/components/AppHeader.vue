<template>
  <header class="app-header">
    <RouterLink class="brand" to="/">
      <span class="brand-mark"></span>
      <span>SSAFY HOME</span>
    </RouterLink>

    <nav class="top-nav">
      <RouterLink to="/news">뉴스</RouterLink>
      <RouterLink to="/foreigners">외국인 히트맵</RouterLink>
      <RouterLink to="/myhome">마이홈</RouterLink>
      <RouterLink to="/favorites">관심</RouterLink>
    </nav>

    <div class="auth-links">
      <RouterLink to="/login">로그인</RouterLink>
      <RouterLink class="primary-link" to="/signup">회원가입</RouterLink>
    </div>
  </header>
</template>
