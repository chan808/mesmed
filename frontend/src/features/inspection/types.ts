import type {
  InspectionResult,
  InspectionSeverity,
} from '../../shared/api/types';

export interface InspectionDetailItem {
  inspectionSpecId: number;
  measuredValue: string | null;
  result: InspectionResult | null;
  severity: InspectionSeverity | null;
}

export interface InspectionCreateRequest {
  lotId: number;
  inspectorId?: number;
  note?: string;
  details: InspectionDetailItem[];
}

export interface InspectionResponse {
  id: number;
  lotId: number;
  lotNo: string;
  inspectorName: string | null;
  overallResult: InspectionResult;
  note: string | null;
  inspectedAt: string;
  details: Array<{
    id: number;
    specId: number;
    itemName: string;
    specDesc: string | null;
    measuredValue: string | null;
    result: InspectionResult;
    severity: InspectionSeverity | null;
  }>;
}
