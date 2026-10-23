import type { ApiResult, AppEventBus, AppSdk } from '@company/contracts';

export interface SdkOptions {
  baseUrl: string;
  getToken?: () => string | null;
  onUnauthorized?: () => void;
}

export class ApiError extends Error {
  constructor(
    readonly code: number,
    message: string,
    readonly requestId?: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

/**
 * 所有微前端共享同一个请求出口：鉴权头、链路 ID、后端 ApiResult 解包只实现一次。
 * 远端拿到的 sdk 由 shell 注入，因此不需要也不允许自己创建 axios 实例。
 */
export class FrontendSdk implements AppSdk {
  constructor(private readonly options: SdkOptions) {}

  get<T>(path: string): Promise<T> {
    return this.request<T>(path, { method: 'GET' });
  }

  post<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body ?? {})
    });
  }

  put<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>(path, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body ?? {})
    });
  }

  del<T>(path: string): Promise<T> {
    return this.request<T>(path, { method: 'DELETE' });
  }

  private async request<T>(path: string, init: RequestInit): Promise<T> {
    const requestId = crypto.randomUUID();
    const headers = new Headers(init.headers);
    headers.set('X-Request-Id', requestId);

    const token = this.options.getToken?.();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }

    const response = await fetch(resolveUrl(this.options.baseUrl, path), { ...init, headers });

    if (response.status === 401) {
      this.options.onUnauthorized?.();
      throw new ApiError(401, '登录已过期，请重新登录', requestId);
    }

    const payload = (await response.json().catch(() => null)) as ApiResult<T> | null;
    if (!payload) {
      throw new ApiError(response.status, `响应无法解析 (HTTP ${response.status})`, requestId);
    }
    if (!response.ok || payload.code !== 0) {
      throw new ApiError(payload.code, payload.message || `请求失败 (HTTP ${response.status})`, requestId);
    }
    return payload.data;
  }
}

function resolveUrl(baseUrl: string, path: string): string {
  if (/^https?:\/\//.test(path)) {
    return path;
  }
  const left = baseUrl.replace(/\/+$/, '');
  const right = path.startsWith('/') ? path : `/${path}`;
  return `${left}${right}`;
}

type Handler = (payload: unknown) => void;

/**
 * 跨微前端通信只允许走事件总线，禁止应用之间直接 import。
 * 放在 sdk 中，保证 shell 注入的实例与远端独立调试时创建的实例是同一套语义。
 */
export class EventBus implements AppEventBus {
  private handlers = new Map<string, Set<Handler>>();

  emit(type: string, payload?: unknown): void {
    const handlers = this.handlers.get(type);
    if (!handlers) {
      return;
    }
    for (const handler of handlers) {
      try {
        handler(payload);
      } catch (error) {
        console.error(`[event-bus] handler error for ${type}`, error);
      }
    }
  }

  on(type: string, handler: Handler): () => void {
    const handlers = this.handlers.get(type) ?? new Set<Handler>();
    handlers.add(handler);
    this.handlers.set(type, handlers);
    return () => handlers.delete(handler);
  }
}
