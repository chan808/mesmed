package com.chan.medmes.inspection.enums;

public enum InspectionSeverity {
    CRITICAL,  // 치명 — 안전·성능에 직결, 즉시 사용 불가
    MAJOR,     // 주요 — 기능에 영향, 재검토 필요
    MINOR      // 경미 — 외관·규격 미세 이탈, 조건부 허용 가능
}