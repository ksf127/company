import { useState } from 'react';
import type { ApiResult } from '@company/contracts';

interface LoginResult {
  token: string;
  expiresInSeconds: number;
}

/**
 * 登录页不用 sdk：sdk 在 401 时会跳转登录页，登录失败会变成死循环。
 * 这里直接请求 /api/auth/login，由 shell 的 dev proxy（生产为网关）转发到 auth-service。
 */
export function LoginPage({ onSuccess }: { onSuccess: () => void }) {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(): Promise<void> {
    if (!username.trim() || !password) {
      setError('请输入账号和密码');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const base = import.meta.env.VITE_API_BASE ?? '/api';
      const response = await fetch(`${base}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: username.trim(), password })
      });
      const payload = (await response.json()) as ApiResult<LoginResult>;
      if (!response.ok || payload.code !== 0 || !payload.data?.token) {
        setError(payload.message || `登录失败 (HTTP ${response.status})`);
        return;
      }
      localStorage.setItem('token', payload.data.token);
      onSuccess();
    } catch (e) {
      setError(e instanceof Error ? e.message : '登录失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>Company Console</h1>
        <p className="login-hint">默认账号 admin / admin123</p>
        <label>
          账号
          <input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" />
        </label>
        <label>
          密码
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                void handleSubmit();
              }
            }}
            autoComplete="current-password"
          />
        </label>
        {error ? <p className="login-error">{error}</p> : null}
        <button type="button" onClick={() => void handleSubmit()} disabled={loading}>
          {loading ? '登录中…' : '登录'}
        </button>
      </div>
    </div>
  );
}
