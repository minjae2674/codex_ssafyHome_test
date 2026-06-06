import { http, unwrapApiResponse } from './http';

export async function searchApartments(params) {
  const response = await http.get('/apartments/search', { params });
  return unwrapApiResponse(response);
}
