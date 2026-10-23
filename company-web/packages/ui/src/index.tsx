import type { CSSProperties, ReactNode } from 'react';

const cardStyle: CSSProperties = {
  background: '#fff',
  border: '1px solid #e6e8ef',
  borderRadius: 12,
  padding: 20
};

export function PageHeader({ title, description, actions }: { title: string; description?: string; actions?: ReactNode }) {
  return (
    <header style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 20 }}>
      <div>
        <h2 style={{ margin: 0, fontSize: 22 }}>{title}</h2>
        {description ? <p style={{ margin: '6px 0 0', color: '#6b7280' }}>{description}</p> : null}
      </div>
      {actions ? <div style={{ display: 'flex', gap: 8 }}>{actions}</div> : null}
    </header>
  );
}

export function Card({ title, children }: { title?: string; children: ReactNode }) {
  return (
    <section style={cardStyle}>
      {title ? <h3 style={{ marginTop: 0, fontSize: 16 }}>{title}</h3> : null}
      {children}
    </section>
  );
}

export function Button({
  children,
  onClick,
  disabled
}: {
  children: ReactNode;
  onClick?: () => void;
  disabled?: boolean;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      style={{
        border: 'none',
        borderRadius: 8,
        padding: '8px 16px',
        background: disabled ? '#c7cbd6' : '#2563eb',
        color: '#fff',
        cursor: disabled ? 'not-allowed' : 'pointer'
      }}
    >
      {children}
    </button>
  );
}

export function StatusTag({ text, tone = 'neutral' }: { text: string; tone?: 'neutral' | 'success' | 'danger' }) {
  const palette = {
    neutral: { background: '#eef2ff', color: '#3730a3' },
    success: { background: '#dcfce7', color: '#166534' },
    danger: { background: '#fee2e2', color: '#991b1b' }
  }[tone];

  return (
    <span style={{ ...palette, borderRadius: 999, padding: '2px 10px', fontSize: 12, fontWeight: 600 }}>{text}</span>
  );
}
