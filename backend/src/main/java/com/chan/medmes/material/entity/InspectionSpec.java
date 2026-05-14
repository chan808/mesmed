package com.chan.medmes.material.entity;

import com.chan.medmes.material.enums.InspectionCategory;
import com.chan.medmes.material.enums.InspectionEquipment;
import com.chan.medmes.material.enums.InspectionMethod;
import com.chan.medmes.material.enums.InspectionTiming;
import com.chan.medmes.material.enums.MeasureType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_spec")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_material_id", nullable = false)
    private RawMaterial rawMaterial;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private InspectionCategory category;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "spec_desc", length = 300)
    private String specDesc;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InspectionMethod method;

    // method == OTHER 일 때 자유 입력
    @Column(name = "method_custom", length = 50)
    private String methodCustom;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private InspectionEquipment equipment;

    @Column(name = "equipment_custom", length = 100)
    private String equipmentCustom;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InspectionTiming timing;

    @Column(name = "timing_custom", length = 50)
    private String timingCustom;

    @Enumerated(EnumType.STRING)
    @Column(name = "measure_type", length = 10, nullable = false)
    private MeasureType measureType;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(length = 20)
    private String unit;

    @Column(nullable = false)
    private int version;

    // null = 현행 기준, not null = 개정/삭제로 대체된 구버전
    @Column(name = "superseded_at")
    private LocalDateTime supersededAt;

    @Builder
    public InspectionSpec(RawMaterial rawMaterial,
                          InspectionCategory category,
                          String itemName, String specDesc,
                          InspectionMethod method, String methodCustom,
                          InspectionEquipment equipment, String equipmentCustom,
                          InspectionTiming timing, String timingCustom,
                          MeasureType measureType, Double minValue, Double maxValue,
                          String unit, Integer version) {
        this.rawMaterial = rawMaterial;
        this.category = (category != null) ? category : InspectionCategory.OTHER;
        this.itemName = itemName;
        this.specDesc = specDesc;
        this.method = method;
        this.methodCustom = (method == InspectionMethod.OTHER) ? methodCustom : null;
        this.equipment = equipment;
        this.equipmentCustom = (equipment == InspectionEquipment.OTHER) ? equipmentCustom : null;
        this.timing = timing;
        this.timingCustom = (timing == InspectionTiming.OTHER) ? timingCustom : null;
        this.measureType = (measureType != null) ? measureType : MeasureType.VISUAL;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
        this.version = (version != null) ? version : 1;
    }

    public void supersede() {
        this.supersededAt = LocalDateTime.now();
    }
}
