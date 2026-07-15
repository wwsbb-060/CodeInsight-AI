import client from './client';
import type { ApiResult } from '@/types';

export interface StatsOverview {
  totalRepos: number;
  totalReviews: number;
  monthReviews: number;
  totalTokens: number;
}

export interface TokenTrendItem {
  date: string;
  tokens: number;
}

export interface ActivityItem {
  id: number;
  repoName: string;
  status: string;
  aiModel: string | null;
  tokenUsed: number;
  createdAt: string;
  errorMsg: string | null;
}

export function getOverview(): Promise<ApiResult<StatsOverview>> {
  return client.get('/stats/overview');
}

export function getTokenTrend(): Promise<ApiResult<TokenTrendItem[]>> {
  return client.get('/stats/tokens');
}

export function getActivity(): Promise<ApiResult<ActivityItem[]>> {
  return client.get('/stats/activity');
}
