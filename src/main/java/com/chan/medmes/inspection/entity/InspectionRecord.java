package com.chan.medmes.inspection.entity;

import com.chan.medmes.inspection.enums.InspectionResult;
import com.chan.medmes.material.entity.Lot;
import com.chan.medmes.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id")
    private User inspector;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_result", length = 10)
    private InspectionResult overallResult;

    @Column(length = 500)
    private String note;

    @Column(name = "inspected_at", updatable = false)
    private LocalDateTime inspectedAt;

    @Builder
    public InspectionRecord(Lot lot, User inspector, String note) {
        this.lot = lot;
        this.inspector = inspector;
        this.note = note;
        this.inspectedAt = LocalDateTime.now();
    }

    public void conclude(InspectionResult result) {
        this.overallResult = result;
    }
}