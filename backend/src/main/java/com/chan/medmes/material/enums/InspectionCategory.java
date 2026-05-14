package com.chan.medmes.material.enums;

// 검사 항목 카테고리. 다른 enum 필드의 추천 default를 보유한다.
// 추천일 뿐 사용자가 다른 값으로 자유롭게 덮어쓸 수 있다.
public enum InspectionCategory {
    DIMENSION   (InspectionMethod.MEASURE,      InspectionEquipment.CALIPER,           MeasureType.NUMERIC, "mm"),
    APPEARANCE  (InspectionMethod.VISUAL_CHECK, InspectionEquipment.VISUAL_INSPECTION, MeasureType.VISUAL,  null),
    COLOR       (InspectionMethod.VISUAL_CHECK, InspectionEquipment.VISUAL_INSPECTION, MeasureType.VISUAL,  null),
    ELECTRICAL  (InspectionMethod.MEASURE,      InspectionEquipment.MULTIMETER,        MeasureType.NUMERIC, "V"),
    PERFORMANCE (InspectionMethod.TEST,         null,                                  MeasureType.VISUAL,  null),
    THERMAL     (InspectionMethod.MEASURE,      InspectionEquipment.THERMAL_CAMERA,    MeasureType.NUMERIC, "℃"),
    OTHER       (null,                          null,                                  null,                null);

    private final InspectionMethod defaultMethod;
    private final InspectionEquipment defaultEquipment;
    private final MeasureType defaultMeasureType;
    private final String defaultUnit;

    InspectionCategory(InspectionMethod defaultMethod,
                       InspectionEquipment defaultEquipment,
                       MeasureType defaultMeasureType,
                       String defaultUnit) {
        this.defaultMethod = defaultMethod;
        this.defaultEquipment = defaultEquipment;
        this.defaultMeasureType = defaultMeasureType;
        this.defaultUnit = defaultUnit;
    }

    public InspectionMethod getDefaultMethod() { return defaultMethod; }
    public InspectionEquipment getDefaultEquipment() { return defaultEquipment; }
    public MeasureType getDefaultMeasureType() { return defaultMeasureType; }
    public String getDefaultUnit() { return defaultUnit; }
}
