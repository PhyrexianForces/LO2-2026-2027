export interface HardwareDevice {
  name: string;
  type: string;
  connectionDetails: string;
  version: number;
}

export type OpModeType = 'AUTONOMOUS' | 'TELEOP';

export interface ConnectionInfo {
  opModeName: string;
  opModeType: OpModeType;
  hardwareDevices: HardwareDevice[];
}

export interface FieldPosition {
  x: number;
  y: number;
  direction: number;
}

export type ActionStatus = 'queued' | 'running' | 'completed' | 'cancelled';

export interface TelemetryAction {
  actionName: string;
  actionParameters: string;
  status: ActionStatus;
  subActions: TelemetryAction[];
}

export interface TelemetryActions {
  actions: TelemetryAction[];
}

export type TelemetryDataType = 'double' | 'string' | 'integer' | 'fieldPosition' | 'robotPosition';

export interface TelemetryDataEntry {
  type: TelemetryDataType;
  value: number | string | FieldPosition | null;
}

export type TelemetryDataMap = Record<string, TelemetryDataEntry>;

export interface TelemetryUpdatePacket {
  _packetType: 'TelemetryUpdatePacket';
  telemetryDataName: string;
  telemetryDataType: string;
  telemetryDataValue: number | string | FieldPosition | TelemetryActions | null;
}

export interface TelemetryNewConnectionPacket {
  _packetType: 'TelemetryNewConnectionPacket';
  opModeName: string;
  opModeType: OpModeType;
  hardwareDevices: HardwareDevice[];
}
