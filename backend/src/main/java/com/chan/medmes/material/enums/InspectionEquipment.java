package com.chan.medmes.material.enums;

public enum InspectionEquipment {
    CALIPER,            // 캘리퍼 (내/외측)
    MICROMETER,         // 마이크로미터
    MULTIMETER,         // 디지털멀티미터
    ECG_SIMULATOR,      // ECG시뮬레이터
    VISUAL_INSPECTION,  // 육안확인
    THERMAL_CAMERA,     // 열화상카메라
    STOPWATCH,          // 스톱워치
    HOST_DEVICE,        // 본체 (장착 후 동작 확인)
    OTHER               // 기타 — equipmentCustom에 자유 입력
}
