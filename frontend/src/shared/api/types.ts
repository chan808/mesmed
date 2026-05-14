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
