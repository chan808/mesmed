package com.chan.medmes.material.entity;

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

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "spec_desc", length = 300)
    private String specDesc;

    @Column(length = 50)
    private String method;

    @Column(length = 100)
    private String equipment;

    @Column(length = 50)
    private String timing;

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
    public InspectionSpec(RawMaterial rawMaterial, String itemName, String specDesc,
                          String method, String equipment, String timing,
                          MeasureType measureType, Double minValue, Double maxValue,
                          String unit, Integer version) {
        this.rawMaterial = rawMaterial;
        this.itemName = itemName;
        this.specDesc = specDesc;
        this.method = method;
        this.equipment = equipment;
        this.timing = timing;
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