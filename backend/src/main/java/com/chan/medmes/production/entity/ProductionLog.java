package com.chan.medmes.production.entity;

import com.chan.medmes.material.entity.Lot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @Column(name = "process_name", length = 100)
    private String processName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Column(name = "produced_qty")
    private Integer producedQty;

    @Column(name = "defect_qty")
    private Integer defectQty;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder
    public ProductionLog(Lot lot, String processName, Equipment equipment,
                         Integer producedQty, Integer defectQty,
                         LocalDateTime startedAt, LocalDateTime endedAt) {
        this.lot = lot;
        this.processName = processName;
        this.equipment = equipment;
        this.producedQty = producedQty;
        this.defectQty = (defectQty != null) ? defectQty : 0;
        this.startedAt = (startedAt != null) ? startedAt : LocalDateTime.now();
        this.endedAt = endedAt;
    }
}