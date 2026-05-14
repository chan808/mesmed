import { api, unwrap } from '../../shared/api/client';
import type { ApiResponse } from '../../shared/api/types';

export interface Dashboard {
  todayProducedQty: number;
  activeAlarmCount: number;
  equipmentRunningRate: number;
  pendingLotCount: number;
}

export const dashboardApi = {
  get: () => unwrap(api.get<ApiResponse<Dashboard>>('/dashboard')),
};
