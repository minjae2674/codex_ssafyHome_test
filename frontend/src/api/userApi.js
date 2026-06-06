import { http, unwrapApiResponse } from './http';

export async function signupUser(payload) {
  const response = await http.post('/users/signup', payload);
  return unwrapApiResponse(response);
}

export async function loginUser(payload) {
  const response = await http.post('/users/login', payload);
  return unwrapApiResponse(response);
}

export async function fetchCurrentUser() {
  const response = await http.get('/users/me');
  return unwrapApiResponse(response);
}
