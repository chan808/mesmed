import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { authApi } from './api';
import { useAuth } from '../../shared/auth/useAuth';
import { errorMessage } from '../../shared/api/client';

export function LoginPage() {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const navigate = useNavigate();
  const { login } = useAuth();

  const mutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      login(data.accessToken, data.userId, data.role);
      navigate('/dashboard');
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate({ username, password });
  };

  return (
    <div className="login-box">
      <h1>medmes MES</h1>
      <form onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="username">아이디</label>
          <input
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="field">
          <label htmlFor="password">비밀번호</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {mutation.isError && (
          <div className="error">{errorMessage(mutation.error)}</div>
        )}
        <button type="submit" className="primary" disabled={mutation.isPending}>
          {mutation.isPending ? '로그인 중…' : '로그인'}
        </button>
        <div className="muted" style={{ fontSize: 12, textAlign: 'center' }}>
          기본 계정: admin / admin123
        </div>
      </form>
    </div>
  );
}
