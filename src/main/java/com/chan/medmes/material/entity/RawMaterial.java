package com.chan.medmes.material.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_material")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RawMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(length = 20)
    private String unit;

    @Column(name = "spec_standard", length = 200)
    private String specStandard;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public RawMaterial(String code, String name, String category, String unit, String specStandard) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.specStandard = specStandard;
        this.createdAt = LocalDateTime.now();
    }
}