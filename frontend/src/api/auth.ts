import client from './client';
import type { ApiResult, LoginRequest, LoginResponse, RegisterRequest } from '@/types';

export function login(data: LoginRequest): Promise<ApiResult<LoginResponse>> {
  return client.post('/auth/login', data);
}

export function register(data: RegisterRequest): Promise<ApiResult<null>> {
  return client.post('/auth/register', data);
}

export function logout(): Promise<ApiResult<null>> {
  return client.post('/auth/logout');
}
