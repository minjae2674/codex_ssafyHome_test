import axios from 'axios';

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api',
  timeout: 10000,
});

http.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem('ssafyHomeAccessToken');

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

export function unwrapApiResponse(response) {
  if (!response.data?.success) {
    throw new Error(response.data?.message || 'API 요청에 실패했습니다.');
  }
  return response.data.data;
}
