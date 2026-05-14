import { createContext, useState, useCallback, type ReactNode } from 'react';
import type { UserRole } from '../api/types';

export interface AuthState {
  token: string | null;
  userId: number | null;
  role: UserRole | null;
}

export interface AuthContextValue extends AuthState {
  login: (token: string, userId: number, role: UserRole) => void;
  logout: () => void;
}

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext<AuthContextValue | null>(null);

function readInitial(): AuthState {
  const token = localStorage.getItem('token');
  const userIdStr = localStorage.getItem('userId');
  const role = localStorage.getItem('role') as UserRole | null;
  return {
    token,
    userId: userIdStr ? Number(userIdStr) : null,
    role,
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>(readInitial);

  const login = useCallback((token: string, userId: number, role: UserRole) => {
    localStorage.setItem('token', token);
    localStorage.setItem('userId', String(userId));
    localStorage.setItem('role', role);
    setState({ token, userId, role });
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('role');
    setState({ token: null, userId: null, role: null });
  }, []);

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
