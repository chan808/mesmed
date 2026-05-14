import type {
  AlarmSeverity,
  EquipmentStatus,
} from '../../shared/api/types';

export interface Equipment {
  id: number;
  equipmentCode: string;
  name: string;
  status: EquipmentStatus;
  lastMaintainedAt: string | null;
}

export interface EquipmentCreateRequest {
  equipmentCode: string;
  name: string;
}

export interface Alarm {
  id: number;
  equipmentId: number;
  equipmentName: string;
  alarmCode: string | null;
  message: string | null;
  severity: AlarmSeverity;
  occurredAt: string;
  resolvedAt: string | null;
  resolved: boolean;
}

export interface ProductionLog {
  id: number;
  lotId: number;
  lotNo: string;
  processName: string | null;
  equipmentId: number | null;
  equipmentName: string | null;
  producedQty: number;
  defectQty: number;
  startedAt: string;
  endedAt: string | null;
}

export interface ProductionLogCreateRequest {
  lotId: number;
  processName: string;
  equipmentId?: number;
  producedQty: number;
  defectQty?: number;
  startedAt?: string;
  endedAt?: string;
}
