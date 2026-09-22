import { useState, useEffect, useRef, useCallback } from 'react';
import type {
  ConnectionInfo,
  FieldPosition,
  TelemetryActions,
  TelemetryDataMap,
  TelemetryDataType,
} from '../types/telemetry';

function getDefaultHost(): string | null {
  if (typeof window === 'undefined') return null;
  if (window.location.protocol === 'file:') return null;
  const hostname = window.location.hostname;
  return hostname && hostname !== '' ? hostname : null;
}

export function useTelemetrySocket() {
  const [connected, setConnected] = useState(false);
  const [connectionInfo, setConnectionInfo] = useState<ConnectionInfo | null>(null);
  const [telemetryData, setTelemetryData] = useState<TelemetryDataMap>({});
  const [fieldPosition, setFieldPosition] = useState<FieldPosition | null>(null);
  const [actions, setActions] = useState<TelemetryActions | null>(null);
  const [ip, setIp] = useState<string>(getDefaultHost() ?? '');

  const wsRef = useRef<WebSocket | null>(null);
  const reconnectRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const activeHost = useRef<string>('');

  const connect = useCallback((host: string) => {
    if (!host) return;
    activeHost.current = host;

    if (reconnectRef.current) {
      clearTimeout(reconnectRef.current);
      reconnectRef.current = null;
    }
    if (wsRef.current) {
      wsRef.current.onclose = null;
      wsRef.current.close();
    }

    const ws = new WebSocket(`ws://${host}:51631`);
    wsRef.current = ws;

    ws.onopen = () => setConnected(true);

    ws.onclose = () => {
      setConnected(false);
      reconnectRef.current = setTimeout(() => {
        if (activeHost.current) connect(activeHost.current);
      }, 3000);
    };

    ws.onerror = () => ws.close();

    ws.onmessage = (event: MessageEvent) => {
      try {
        const packet = JSON.parse(event.data as string);
        if (packet._packetType === 'TelemetryNewConnectionPacket') {
          setConnectionInfo({
            opModeName: packet.opModeName,
            opModeType: packet.opModeType,
            hardwareDevices: packet.hardwareDevices ?? [],
          });
          setTelemetryData({});
          setFieldPosition(null);
          setActions(null);
        } else if (packet._packetType === 'TelemetryUpdatePacket') {
          const { telemetryDataName, telemetryDataType, telemetryDataValue } = packet;
          if (telemetryDataType === 'robotPosition') {
            setFieldPosition(telemetryDataValue as FieldPosition);
            setTelemetryData(prev => ({
              ...prev,
              [telemetryDataName]: {
                type: 'robotPosition',
                value: telemetryDataValue as FieldPosition | null,
              },
            }));
          } else if (telemetryDataType === 'actions') {
            setActions(telemetryDataValue as TelemetryActions);
          } else {
            setTelemetryData(prev => ({
              ...prev,
              [telemetryDataName]: {
                type: telemetryDataType as TelemetryDataType,
                value: telemetryDataValue as number | string | FieldPosition | null,
              },
            }));
          }
        }
      } catch {
        // ignore malformed packets
      }
    };
  }, []);

  useEffect(() => {
    const host = getDefaultHost();
    if (host) {
      setIp(host);
      connect(host);
    }
    return () => {
      activeHost.current = '';
      if (reconnectRef.current) clearTimeout(reconnectRef.current);
      if (wsRef.current) {
        wsRef.current.onclose = null;
        wsRef.current.close();
      }
    };
  }, [connect]);

  return { connected, connectionInfo, telemetryData, fieldPosition, actions, ip, setIp, connect };
}
