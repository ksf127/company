import { useEffect, useState, type ComponentType } from 'react';
import type { AppEventBus, AppManifest, AppSdk, RemoteAppProps } from '@company/contracts';
import { RemoteErrorBoundary } from './error-boundary';
import { loadRemoteApp } from './remote';

interface Props {
  app: AppManifest;
  sdk: AppSdk;
  eventBus: AppEventBus;
}

/**
 * shell 与远端之间唯一的耦合点：加载 + 注入上下文 + 隔离故障。
 */
export function RemoteHost({ app, sdk, eventBus }: Props) {
  const [component, setComponent] = useState<ComponentType<RemoteAppProps> | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setComponent(null);
    setError(null);

    loadRemoteApp(app)
      .then((loaded) => {
        if (!cancelled) {
          setComponent(() => loaded);
        }
      })
      .catch((loadError: Error) => {
        if (!cancelled) {
          setError(loadError);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [app, attempt]);

  if (error) {
    return (
      <div className="remote-state">
        <p>「{app.name}」加载失败</p>
        <pre>{error.message}</pre>
        <button type="button" onClick={() => setAttempt((value) => value + 1)}>
          重试
        </button>
      </div>
    );
  }

  if (!component) {
    return <div className="remote-state">正在加载「{app.name}」…</div>;
  }

  const RemoteApp = component;
  return (
    <RemoteErrorBoundary
      fallback={(renderError, reset) => (
        <div className="remote-state">
          <p>「{app.name}」运行时异常</p>
          <pre>{renderError.message}</pre>
          <button
            type="button"
            onClick={() => {
              reset();
              setAttempt((value) => value + 1);
            }}
          >
            重新加载
          </button>
        </div>
      )}
    >
      <RemoteApp appId={app.id} basename={app.route} sdk={sdk} eventBus={eventBus} />
    </RemoteErrorBoundary>
  );
}
