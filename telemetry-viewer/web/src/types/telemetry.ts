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

export interface TelemetryAction {
  actionName: string;
  actionParameters: string;
  subActions: TelemetryAction[];
}

export interface ActionQueue {
  actionQueue: TelemetryAction[];
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
  telemetryDataValue: number | string | FieldPosition | ActionQueue | null;
}

export interface TelemetryNewConnectionPacket {
  _packetType: 'TelemetryNewConnectionPacket';
  opModeName: string;
  opModeType: OpModeType;
  hardwareDevices: HardwareDevice[];
}
