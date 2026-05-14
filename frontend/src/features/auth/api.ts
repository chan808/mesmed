import { api, unwrap } from '../../shared/api/client';
import type { ApiResponse, UserRole } from '../../shared/api/types';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  userId: number;
  role: UserRole;
}

export const authApi = {
  login: (body: LoginRequest) =>
    unwrap(api.post<ApiResponse<LoginResponse>>('/auth/login', body)),
};
