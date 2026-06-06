import { http, unwrapApiResponse } from './http';

export async function fetchSidos() {
  const response = await http.get('/regions/sidos');
  return unwrapApiResponse(response);
}

export async function fetchGuguns(sidoName) {
  const response = await http.get('/regions/guguns', {
    params: { sidoName },
  });
  return unwrapApiResponse(response);
}

export async function fetchDongs(sidoName, gugunName) {
  const response = await http.get('/regions/dongs', {
    params: { sidoName, gugunName },
  });
  return unwrapApiResponse(response);
}
