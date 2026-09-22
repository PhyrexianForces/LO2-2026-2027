import type { FieldPosition, TelemetryDataMap } from '../types/telemetry';
import { useTheme } from '../theme';

interface Props {
  telemetryData: TelemetryDataMap;
}

function isFieldPosition(value: unknown): value is FieldPosition {
  return (
    typeof value === 'object' &&
    value !== null &&
    'x' in value &&
    'y' in value &&
    'direction' in value
  );
}

function formatFieldPosition(position: FieldPosition): string {
  return `(${position.x.toFixed(2)}, ${position.y.toFixed(2)}, ${position.direction.toFixed(2)} rad)`;
}

export function TelemetryDataPanel({ telemetryData }: Props) {
  const { theme } = useTheme();
  const entries = Object.entries(telemetryData);

  return (
    <div style={{ background: theme.bgPanel, overflow: 'auto', padding: 12, borderRadius: 4 }}>
      <div style={{ color: theme.textTertiary, marginBottom: 8, fontSize: 11, textTransform: 'uppercase', letterSpacing: 1 }}>
        Telemetry Data
      </div>
      {entries.length === 0 ? (
        <div style={{ color: theme.textFaded, fontSize: 12 }}>No data</div>
      ) : (
        entries.map(([key, { type, value }]) => (
          <div
            key={key}
            style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 3, gap: 12, fontSize: 13 }}
          >
            <span style={{ color: theme.textLabel }}>{key}</span>
            <span style={{ color: type === 'double' || type === 'integer' ? theme.colorNumeric : theme.colorString }}>
              {value == null
                ? 'null'
                : typeof value === 'number'
                ? value.toFixed(4)
                : isFieldPosition(value)
                ? formatFieldPosition(value)
                : String(value)}
            </span>
          </div>
        ))
      )}
    </div>
  );
}
