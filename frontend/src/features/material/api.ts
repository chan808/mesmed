import { api, unwrap } from '../../shared/api/client';
import type { ApiResponse } from '../../shared/api/types';
import type {
  InspectionSpec,
  InspectionSpecCreateRequest,
  Lot,
  LotCreateRequest,
  LotHistory,
  RawMaterial,
  RawMaterialCreateRequest,
} from './types';

export const materialApi = {
  list: () => unwrap(api.get<ApiResponse<RawMaterial[]>>('/materials')),
  detail: (id: number) =>
    unwrap(api.get<ApiResponse<RawMaterial>>(`/materials/${id}`)),
  create: (body: RawMaterialCreateRequest) =>
    unwrap(api.post<ApiResponse<RawMaterial>>('/materials', body)),
  remove: (id: number) =>
    unwrap(api.delete<ApiResponse<void>>(`/materials/${id}`)),
  specs: (materialId: number) =>
    unwrap(
      api.get<ApiResponse<InspectionSpec[]>>(`/materials/${materialId}/specs`),
    ),
};

export const specApi = {
  create: (body: InspectionSpecCreateRequest) =>
    unwrap(api.post<ApiResponse<InspectionSpec>>('/materials/specs', body)),
  update: (id: number, body: InspectionSpecCreateRequest) =>
    unwrap(api.patch<ApiResponse<InspectionSpec>>(`/materials/specs/${id}`, body)),
  remove: (id: number) =>
    unwrap(api.delete<ApiResponse<void>>(`/materials/specs/${id}`)),
};

export const lotApi = {
  list: () => unwrap(api.get<ApiResponse<Lot[]>>('/lots')),
  detail: (id: number) => unwrap(api.get<ApiResponse<Lot>>(`/lots/${id}`)),
  history: (id: number) =>
    unwrap(api.get<ApiResponse<LotHistory>>(`/lots/${id}/history`)),
  create: (body: LotCreateRequest) =>
    unwrap(api.post<ApiResponse<Lot>>('/lots', body)),
};
