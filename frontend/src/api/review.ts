import client from './client';
import type { ApiResult, CreateReviewRequest, ReviewVO } from '@/types';

export function createReview(data: CreateReviewRequest): Promise<ApiResult<ReviewVO>> {
  return client.post('/reviews', data);
}

export function getReviewList(): Promise<ApiResult<ReviewVO[]>> {
  return client.get('/reviews');
}

export function getReviewById(id: number): Promise<ApiResult<ReviewVO>> {
  return client.get(`/reviews/${id}`);
}

export function deleteReview(id: number): Promise<ApiResult<null>> {
  return client.delete(`/reviews/${id}`);
}

export function updateMemo(id: number, memo: string): Promise<ApiResult<null>> {
  return client.put(`/reviews/${id}/memo`, { memo });
}

export function getReviewReport(id: number): Promise<string> {
  return client.get(`/reviews/${id}/report`, { responseType: 'text' }) as unknown as Promise<string>;
}
