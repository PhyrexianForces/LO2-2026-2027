import { useEffect } from 'react';
import { useTelemetrySocket } from './hooks/useTelemetrySocket';
import { ConnectionPanel } from './components/ConnectionPanel';
import { TelemetryDataPanel } from './components/TelemetryDataPanel';
import { RobotInfoPanel } from './components/RobotInfoPanel';
import { FieldViewer3D } from './components/FieldViewer3D';
import { ActionsPanel } from './components/ActionsPanel';
import { useTheme } from './theme';

export function App() {
  const {
    connected,
    connectionInfo,
    telemetryData,
    fieldPosition,
    actions,
    ip,
    setIp,
    connect,
  } = useTelemetrySocket();

  const { theme } = useTheme();

  useEffect(() => {
    document.body.style.background = theme.bgMain;
    document.body.style.color = theme.textPrimary;
  }, [theme]);

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      background: theme.bgMain,
      color: theme.textPrimary,
      fontFamily: 'monospace',
    }}>
      <ConnectionPanel
        connected={connected}
        connectionInfo={connectionInfo}
        ip={ip}
        setIp={setIp}
        onConnect={connect}
      />
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 2fr',
        gridTemplateRows: '1fr 1fr',
        flex: 1,
        gap: 4,
        padding: 4,
        overflow: 'hidden',
        minHeight: 0,
      }}>
        <TelemetryDataPanel telemetryData={telemetryData} />
        <FieldViewer3D fieldPosition={fieldPosition} />
        <RobotInfoPanel connectionInfo={connectionInfo} />
        <ActionsPanel actions={actions} />
      </div>
    </div>
  );
}
