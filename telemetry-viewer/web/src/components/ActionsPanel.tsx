import type { ActionStatus, TelemetryAction, TelemetryActions } from '../types/telemetry';
import { useTheme } from '../theme';

const STATUS_GLYPH: Record<ActionStatus, string> = {
  running: '▶',
  queued: '·',
  completed: '✓',
  cancelled: '✕',
};

interface ActionNodeProps {
  action: TelemetryAction;
  depth: number;
}

function ActionNode({ action, depth }: ActionNodeProps) {
  const { theme } = useTheme();
  const status = action.status ?? 'queued';
  const isRunning = status === 'running';
  const isFinished = status === 'completed' || status === 'cancelled';
  const isSimultaneous = action.actionName.toLowerCase().includes('simultaneous');

  let color = theme.colorWaiting;
  if (isRunning) color = theme.colorConnected;
  else if (isFinished) color = theme.textTertiary;
  else if (isSimultaneous) color = theme.colorSimultaneous;

  let border = theme.borderMuted;
  if (isRunning) border = theme.colorConnected;
  else if (!isFinished && isSimultaneous) border = theme.colorSimultaneous;

  return (
    <div style={{ paddingLeft: depth * 14 }}>
      <div style={{
        padding: '2px 6px',
        marginBottom: 2,
        background: isRunning ? theme.bgActiveAction : 'transparent',
        borderLeft: '2px solid ' + border,
        display: 'flex',
        alignItems: 'baseline',
        gap: 4,
        fontSize: 12,
        opacity: isFinished ? 0.6 : 1,
      }}>
        <span style={{ color: isRunning ? theme.colorConnected : theme.textTertiary, fontSize: 10, userSelect: 'none', width: 10 }}>
          {STATUS_GLYPH[status]}
        </span>
        <span style={{ color, textDecoration: status === 'cancelled' ? 'line-through' : 'none' }}>{action.actionName}</span>
        <span style={{ color: theme.textTertiary, fontSize: 11 }}>{action.actionParameters}</span>
        <span style={{ color: isRunning ? theme.colorConnected : theme.textTertiary, fontSize: 10, marginLeft: 'auto' }}>{status}</span>
      </div>
      {action.subActions?.map((sub, i) => (
        <ActionNode key={i} action={sub} depth={depth + 1} />
      ))}
    </div>
  );
}

function countByStatus(actions: TelemetryAction[]): Record<ActionStatus, number> {
  const counts: Record<ActionStatus, number> = { running: 0, queued: 0, completed: 0, cancelled: 0 };
  for (const action of actions) {
    counts[action.status ?? 'queued'] += 1;
  }
  return counts;
}

interface Props {
  actions: TelemetryActions | null;
}

export function ActionsPanel({ actions }: Props) {
  const { theme } = useTheme();
  const list = actions?.actions ?? [];
  const counts = countByStatus(list);
  const summary = [
    counts.running ? `${counts.running} running` : null,
    counts.queued ? `${counts.queued} queued` : null,
    counts.completed ? `${counts.completed} done` : null,
    counts.cancelled ? `${counts.cancelled} cancelled` : null,
  ].filter(Boolean).join(' · ');

  return (
    <div style={{ background: theme.bgPanel, overflow: 'auto', padding: 12, borderRadius: 4 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: 11, color: theme.textTertiary }}>
        <span style={{ textTransform: 'uppercase', letterSpacing: 1 }}>Actions</span>
        <span>{summary}</span>
      </div>
      {!actions ? (
        <div style={{ color: theme.textFaded, fontSize: 12 }}>No actions reported</div>
      ) : list.length === 0 ? (
        <div style={{ color: theme.textFaded, fontSize: 12 }}>Nothing running</div>
      ) : (
        list.map((action, i) => (
          <ActionNode key={i} action={action} depth={0} />
        ))
      )}
    </div>
  );
}
