import type { ConnectionInfo } from '../types/telemetry';
import { useTheme } from '../theme';

interface Props {
  connectionInfo: ConnectionInfo | null;
}

export function RobotInfoPanel({ connectionInfo }: Props) {
  const { theme } = useTheme();

  return (
    <div style={{ background: theme.bgPanel, overflow: 'auto', padding: 12, borderRadius: 4 }}>
      <div style={{ color: theme.textTertiary, marginBottom: 8, fontSize: 11, textTransform: 'uppercase', letterSpacing: 1 }}>
        Hardware Devices
      </div>
      {!connectionInfo ? (
        <div style={{ color: theme.textFaded, fontSize: 12 }}>Not connected</div>
      ) : connectionInfo.hardwareDevices.length === 0 ? (
        <div style={{ color: theme.textFaded, fontSize: 12 }}>No devices</div>
      ) : (
        connectionInfo.hardwareDevices.map((device, i) => (
          <div
            key={i}
            style={{ marginBottom: 8, paddingBottom: 8, borderBottom: '1px solid ' + theme.bgSecondary, fontSize: 12 }}
          >
            <div style={{ color: theme.textPrimary, fontWeight: 'bold' }}>{device.name}</div>
            <div style={{ color: theme.textSecondary, fontSize: 11 }}>{device.type}</div>
            <div style={{ color: theme.textTertiary, fontSize: 11 }}>{device.connectionDetails}</div>
          </div>
        ))
      )}
    </div>
  );
}
