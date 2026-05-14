export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export type LotStatus = 'PENDING' | 'PASS' | 'FAIL' | 'HOLD';
export type InspectionResult = 'PASS' | 'FAIL';
export type InspectionSeverity = 'CRITICAL' | 'MAJOR' | 'MINOR';
export type MeasureType = 'NUMERIC' | 'VISUAL';
export type EquipmentStatus = 'RUNNING' | 'IDLE' | 'ERROR' | 'MAINTENANCE';
export type AlarmSeverity = 'INFO' | 'WARNING' | 'CRITICAL';
export type UserRole = 'ADMIN' | 'INSPECTOR' | 'OPERATOR';

// ── 검사기준 분류 enum (백엔드 material/enums와 매칭) ─────────
export type InspectionCategory =
  | 'DIMENSION'
  | 'APPEARANCE'
  | 'COLOR'
  | 'ELECTRICAL'
  | 'PERFORMANCE'
  | 'THERMAL'
  | 'OTHER';

export type InspectionMethod = 'MEASURE' | 'VISUAL_CHECK' | 'TEST' | 'OTHER';

export type InspectionEquipment =
  | 'CALIPER'
  | 'MICROMETER'
  | 'MULTIMETER'
  | 'ECG_SIMULATOR'
  | 'VISUAL_INSPECTION'
  | 'THERMAL_CAMERA'
  | 'STOPWATCH'
  | 'HOST_DEVICE'
  | 'OTHER';

export type InspectionTiming = 'FULL' | 'SAMPLE' | 'OTHER';

// ── 한글 라벨 매핑 ─────────────────────────────────────────
export const CATEGORY_LABEL: Record<InspectionCategory, string> = {
  DIMENSION: '치수',
  APPEARANCE: '외관',
  COLOR: '발색',
  ELECTRICAL: '전기특성',
  PERFORMANCE: '작동성능',
  THERMAL: '발열',
  OTHER: '기타',
};

export const METHOD_LABEL: Record<InspectionMethod, string> = {
  MEASURE: '측정',
  VISUAL_CHECK: '육안',
  TEST: 'TEST',
  OTHER: '기타',
};

export const EQUIPMENT_LABEL: Record<InspectionEquipment, string> = {
  CALIPER: '캘리퍼',
  MICROMETER: '마이크로미터',
  MULTIMETER: '디지털멀티미터',
  ECG_SIMULATOR: 'ECG시뮬레이터',
  VISUAL_INSPECTION: '육안확인',
  THERMAL_CAMERA: '열화상카메라',
  STOPWATCH: '스톱워치',
  HOST_DEVICE: '본체',
  OTHER: '기타',
};

export const TIMING_LABEL: Record<InspectionTiming, string> = {
  FULL: '전수',
  SAMPLE: '샘플',
  OTHER: '기타',
};

// ── 카테고리별 추천 default (백엔드 InspectionCategory와 매칭) ─────────
export interface CategoryPreset {
  method: InspectionMethod | null;
  equipment: InspectionEquipment | null;
  measureType: MeasureType | null;
  unit: string | null;
}

export const CATEGORY_PRESET: Record<InspectionCategory, CategoryPreset> = {
  DIMENSION:   { method: 'MEASURE',      equipment: 'CALIPER',           measureType: 'NUMERIC', unit: 'mm' },
  APPEARANCE:  { method: 'VISUAL_CHECK', equipment: 'VISUAL_INSPECTION', measureType: 'VISUAL',  unit: null },
  COLOR:       { method: 'VISUAL_CHECK', equipment: 'VISUAL_INSPECTION', measureType: 'VISUAL',  unit: null },
  ELECTRICAL:  { method: 'MEASURE',      equipment: 'MULTIMETER',        measureType: 'NUMERIC', unit: 'V' },
  PERFORMANCE: { method: 'TEST',         equipment: null,                measureType: 'VISUAL',  unit: null },
  THERMAL:     { method: 'MEASURE',      equipment: 'THERMAL_CAMERA',    measureType: 'NUMERIC', unit: '℃' },
  OTHER:       { method: null,           equipment: null,                measureType: null,      unit: null },
};
