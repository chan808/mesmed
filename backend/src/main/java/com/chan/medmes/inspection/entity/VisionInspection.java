package com.chan.medmes.inspection.entity;

import com.chan.medmes.material.entity.Lot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vision_inspection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VisionInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @Column(name = "measured_length")
    private Float measuredLength;

    @Column(name = "measured_hole_size")
    private Float measuredHoleSize;

    @Column(name = "defect_result", length = 10)
    private String defectResult;

    @Column(name = "accuracy_score")
    private Float accuracyScore;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "inspected_at", updatable = false)
    private LocalDateTime inspectedAt;

    @Builder
    public VisionInspection(Lot lot, Float measuredLength, Float measuredHoleSize,
                             String defectResult, Float accuracyScore, String imagePath) {
        this.lot = lot;
        this.measuredLength = measuredLength;
        this.measuredHoleSize = measuredHoleSize;
        this.defectResult = defectResult;
        this.accuracyScore = accuracyScore;
        this.imagePath = imagePath;
        this.inspectedAt = LocalDateTime.now();
    }
}