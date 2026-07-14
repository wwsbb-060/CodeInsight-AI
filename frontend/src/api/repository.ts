import client from './client';
import type { ApiResult, CreateRepoRequest, RepositoryVO } from '@/types';

export function createRepo(data: CreateRepoRequest): Promise<ApiResult<RepositoryVO>> {
  return client.post('/repositories', data);
}

export function getRepoList(): Promise<ApiResult<RepositoryVO[]>> {
  return client.get('/repositories');
}

export function getRepoById(id: number): Promise<ApiResult<RepositoryVO>> {
  return client.get(`/repositories/${id}`);
}

export function deleteRepo(id: number): Promise<ApiResult<null>> {
  return client.delete(`/repositories/${id}`);
}
