import { api, unwrap } from '../../shared/api/client';
import type { ApiResponse, EquipmentStatus } from '../../shared/api/types';
import type {
  Alarm,
  Equipment,
  EquipmentCreateRequest,
  ProductionLog,
  ProductionLogCreateRequest,
} from './types';

export const equipmentApi = {
  list: () => unwrap(api.get<ApiResponse<Equipment[]>>('/equipment')),
  create: (body: EquipmentCreateRequest) =>
    unwrap(api.post<ApiResponse<Equipment>>('/equipment', body)),
  updateStatus: (id: number, status: EquipmentStatus) =>
    unwrap(
      api.patch<ApiResponse<Equipment>>(`/equipment/${id}/status`, { status }),
    ),
};

export const alarmApi = {
  list: (activeOnly = false) =>
    unwrap(
      api.get<ApiResponse<Alarm[]>>(`/alarms${activeOnly ? '?activeOnly=true' : ''}`),
    ),
  resolve: (id: number) =>
    unwrap(api.patch<ApiResponse<Alarm>>(`/alarms/${id}/resolve`)),
};

export const productionLogApi = {
  list: (todayOnly = false) =>
    unwrap(
      api.get<ApiResponse<ProductionLog[]>>(
        `/production-logs${todayOnly ? '?todayOnly=true' : ''}`,
      ),
    ),
  create: (body: ProductionLogCreateRequest) =>
    unwrap(api.post<ApiResponse<ProductionLog>>('/production-logs', body)),
};
