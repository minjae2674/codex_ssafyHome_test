import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import { fetchCurrentUser, loginUser, signupUser } from '@/api/userApi';

const TOKEN_STORAGE_KEY = 'ssafyHomeAccessToken';
const USER_STORAGE_KEY = 'ssafyHomeUser';

function readStoredUser() {
  const storedUser = localStorage.getItem(USER_STORAGE_KEY);

  if (!storedUser) {
    return null;
  }

  try {
    return JSON.parse(storedUser);
  } catch {
    localStorage.removeItem(USER_STORAGE_KEY);
    return null;
  }
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem(TOKEN_STORAGE_KEY) || '');
  const user = ref(readStoredUser());
  const isLoading = ref(false);

  const isAuthenticated = computed(() => Boolean(accessToken.value && user.value));
  const nickname = computed(() => user.value?.nickname || '');

  function saveAuth(authResponse) {
    accessToken.value = authResponse.accessToken;
    user.value = authResponse.user;

    localStorage.setItem(TOKEN_STORAGE_KEY, authResponse.accessToken);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(authResponse.user));
  }

  async function signup(payload) {
    isLoading.value = true;

    try {
      const authResponse = await signupUser(payload);
      saveAuth(authResponse);
      return authResponse.user;
    } finally {
      isLoading.value = false;
    }
  }

  async function login(payload) {
    isLoading.value = true;

    try {
      const authResponse = await loginUser(payload);
      saveAuth(authResponse);
      return authResponse.user;
    } finally {
      isLoading.value = false;
    }
  }

  async function loadCurrentUser() {
    if (!accessToken.value) {
      return null;
    }

    try {
      const currentUser = await fetchCurrentUser();
      user.value = currentUser;
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(currentUser));
      return currentUser;
    } catch (error) {
      logout();
      return null;
    }
  }

  function logout() {
    accessToken.value = '';
    user.value = null;
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
  }

  return {
    accessToken,
    user,
    isLoading,
    isAuthenticated,
    nickname,
    signup,
    login,
    loadCurrentUser,
    logout,
  };
});
