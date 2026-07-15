import client from './client';
import type { ApiResult, QaRequest, QaResponse } from '@/types';

export function askQuestion(reviewId: number, data: QaRequest): Promise<ApiResult<QaResponse>> {
  return client.post(`/reviews/${reviewId}/qa`, data);
}

export function isQaReady(reviewId: number): Promise<ApiResult<boolean>> {
  return client.get(`/reviews/${reviewId}/qa/ready`);
}
