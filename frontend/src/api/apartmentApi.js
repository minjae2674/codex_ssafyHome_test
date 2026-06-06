import { http, unwrapApiResponse } from './http';

export async function searchApartments(params) {
  const response = await http.get('/apartments/search', { params });
  return unwrapApiResponse(response);
}

export async function fetchApartmentDetail(aptSeq) {
  const response = await http.get(`/apartments/${aptSeq}`);
  return unwrapApiResponse(response);
}
