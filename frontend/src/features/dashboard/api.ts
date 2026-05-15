import { api, unwrap } from '../../shared/api/client';
import type { ApiResponse } from '../../shared/api/types';

export interface Dashboard {
  todayProducedQty: number;
  activeAlarmCount: number;
  equipmentRunningRate: number;
  pendingLotCount: number;
  runningEquipmentCount: number;
  totalEquipmentCount: number;
  passLotCount: number;
  failLotCount: number;
  holdLotCount: number;
}

export interface DailyProduction {
  date: string;
  qty: number;
}

export const dashboardApi = {
  get: () => unwrap(api.get<ApiResponse<Dashboard>>('/dashboard')),
  getDailyProduction: () =>
    unwrap(api.get<ApiResponse<DailyProduction[]>>('/dashboard/daily-production')),
};