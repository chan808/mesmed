import axios, { AxiosError } from 'axios';
import type { ApiResponse } from './types';

export const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err: AxiosError<ApiResponse<unknown>>) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('role');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  },
);

// 백엔드는 항상 ApiResponse<T> 포맷. data 필드만 꺼내 쓰기 위한 헬퍼.
export function unwrap<T>(p: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  return p.then((r) => r.data.data);
}

export function errorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const msg = (err.response?.data as ApiResponse<unknown> | undefined)?.message;
    if (msg) return msg;
    return err.message;
  }
  if (err instanceof Error) return err.message;
  return '알 수 없는 오류';
}
