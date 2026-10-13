export interface PilotApply {
  applyNo: string;
  shipName: string;
  shipNetTonnage: number;
  applyTime: string;
  status: number;
  agentName: string;
}

export interface PilotPlan {
  planNo: string;
  shipName: string;
  pilotName: string;
  startTime: string;
  endTime: string;
  status: 'pending' | 'running' | 'done';
}

export interface RealTimePilotData {
  activePlans: number;
  completedToday: number;
  alerts: number;
  operations: PilotPlan[];
}

export interface ShipLocation {
  id: string;
  name: string;
  lnglat: [number, number];
  status: number;
}

export interface TrendChartData {
  months: string[];
  counts: number[];
}

