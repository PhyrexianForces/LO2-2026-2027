import type { ConnectionInfo } from '../types/telemetry';
import { useTheme } from '../theme';

interface Props {
  connected: boolean;
  connectionInfo: ConnectionInfo | null;
  ip: string;
  setIp: (ip: string) => void;
  onConnect: (ip: string) => void;
}

export function ConnectionPanel({ connected, connectionInfo, ip, setIp, onConnect }: Props) {
  const { theme, toggleTheme } = useTheme();

  return (
    <div style={{
      background: theme.bgSecondary,
      padding: '8px 16px',
      borderBottom: '1px solid ' + theme.borderMuted,
      display: 'flex',
      alignItems: 'center',
      gap: 16,
      flexShrink: 0,
    }}>
      <span style={{ color: connected ? theme.colorConnected : theme.colorDisconnected, fontWeight: 'bold', minWidth: 130 }}>
        {connected ? '● CONNECTED' : '○ DISCONNECTED'}
      </span>

      {connectionInfo && (
        <span>
          <strong style={{ color: theme.textPrimary }}>{connectionInfo.opModeName}</strong>
          <span style={{ color: theme.textSecondary, marginLeft: 8 }}>
            ({connectionInfo.opModeType === 'AUTONOMOUS' ? 'Auto' : 'TeleOp'})
          </span>
        </span>
      )}

      <div style={{ marginLeft: 'auto', display: 'flex', gap: 8, alignItems: 'center' }}>
        <span style={{ color: theme.textTertiary, fontSize: 11 }}>Robot IP:</span>
        <input
          value={ip}
          onChange={e => setIp(e.target.value)}
          placeholder="192.168.43.1"
          style={{
            background: theme.bgInput,
            color: theme.textPrimary,
            border: '1px solid ' + theme.borderStrong,
            padding: '3px 8px',
            fontFamily: 'monospace',
            fontSize: 13,
            width: 140,
          }}
          onKeyDown={e => e.key === 'Enter' && onConnect(ip)}
        />
        <button
          onClick={() => onConnect(ip)}
          style={{
            background: theme.bgConnectBtn,
            color: theme.colorConnected,
            border: '1px solid ' + theme.borderConnectBtn,
            padding: '3px 14px',
            cursor: 'pointer',
            fontFamily: 'monospace',
            fontSize: 13,
          }}
        >
          Connect
        </button>
        <button
          onClick={toggleTheme}
          title={theme.mode === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
          style={{
            background: 'transparent',
            color: theme.textSecondary,
            border: '1px solid ' + theme.borderMuted,
            padding: '3px 8px',
            cursor: 'pointer',
            fontFamily: 'monospace',
            fontSize: 13,
          }}
        >
          {theme.mode === 'dark' ? '☀' : '☾'}
        </button>
      </div>
    </div>
  );
}
