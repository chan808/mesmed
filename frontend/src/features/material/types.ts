import type {
  InspectionCategory,
  InspectionEquipment,
  InspectionMethod,
  InspectionResult,
  InspectionTiming,
  LotStatus,
  MeasureType,
} from '../../shared/api/types';

export interface RawMaterial {
  id: number;
  code: string;
  name: string;
  category: string | null;
  unit: string | null;
  specStandard: string | null;
  createdAt: string;
}

export interface RawMaterialCreateRequest {
  code: string;
  name: string;
  category?: string;
  unit?: string;
  specStandard?: string;
}

export interface Lot {
  id: number;
  lotNo: string;
  rawMaterialId: number;
  rawMaterialCode: string;
  rawMaterialName: string;
  quantity: number;
  receivedAt: string;
  supplier: string | null;
  status: LotStatus;
}

export interface InspectionSpec {
  id: number;
  rawMaterialId: number;
  rawMaterialName: string;
  category: InspectionCategory;
  itemName: string;
  specDesc: string | null;
  method: InspectionMethod | null;
  methodCustom: string | null;
  equipment: InspectionEquipment | null;
  equipmentCustom: string | null;
  timing: InspectionTiming | null;
  timingCustom: string | null;
  measureType: MeasureType;
  minValue: number | null;
  maxValue: number | null;
  unit: string | null;
  version: number;
}

export interface InspectionSpecCreateRequest {
  rawMaterialId: number;
  category?: InspectionCategory;
  itemName: string;
  specDesc?: string;
  method?: InspectionMethod;
  methodCustom?: string;
  equipment?: InspectionEquipment;
  equipmentCustom?: string;
  timing?: InspectionTiming;
  timingCustom?: string;
  measureType?: MeasureType;
  minValue?: number;
  maxValue?: number;
  unit?: string;
}

export interface LotHistory {
  lot: {
    lotNo: string;
    rawMaterialCode: string;
    rawMaterialName: string;
    quantity: number;
    receivedAt: string;
    supplier: string | null;
    status: LotStatus;
  };
  inspectionRecords: Array<{
    recordId: number;
    overallResult: InspectionResult | null;
    inspectedAt: string;
    inspectorName: string | null;
    note: string | null;
    details: Array<{
      itemName: string;
      specDesc: string | null;
      measuredValue: string | null;
      result: string;
      severity: string | null;
    }>;
  }>;
  productionLogs: Array<{
    logId: number;
    processName: string | null;
    equipmentName: string | null;
    producedQty: number;
    defectQty: number;
    startedAt: string;
    endedAt: string | null;
  }>;
}

export interface LotCreateRequest {
  rawMaterialId: number;
  lotNo?: string;
  quantity: number;
  receivedAt?: string;
  supplier?: string;
}
