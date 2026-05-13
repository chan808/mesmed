package com.chan.medmes.material.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Builder
    public InspectionSpec(RawMaterial rawMaterial, String itemName, String specDesc,
                          String method, String equipment, String timing) {
        this.rawMaterial = rawMaterial;
        this.itemName = itemName;
        this.specDesc = specDesc;
        this.method = method;
        this.equipment = equipment;
        this.timing = timing;
    }
}