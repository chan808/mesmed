import { api, unwrap } from '../../shared/api/client';
import type { ApiResponse } from '../../shared/api/types';
import type { InspectionCreateRequest, InspectionResponse } from './types';

export const inspectionApi = {
  create: (body: InspectionCreateRequest) =>
    unwrap(api.post<ApiResponse<InspectionResponse>>('/inspections', body)),
};
