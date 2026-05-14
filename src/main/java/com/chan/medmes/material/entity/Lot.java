package com.chan.medmes.material.entity;

import com.chan.medmes.global.error.BusinessException;
import com.chan.medmes.material.LotStatus;
import com.chan.medmes.material.MaterialErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lot_no", unique = true, nullable = false, length = 50)
    private String lotNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_material_id", nullable = false)
    private RawMaterial rawMaterial;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(length = 100)
    private String supplier;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private LotStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Lot(String lotNo, RawMaterial rawMaterial, Integer quantity,
               LocalDateTime receivedAt, String supplier) {
        this.lotNo = lotNo;
        this.rawMaterial = rawMaterial;
        this.quantity = quantity;
        this.receivedAt = (receivedAt != null) ? receivedAt : LocalDateTime.now();
        this.supplier = supplier;
        this.status = LotStatus.PENDING;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 관리자 수동 전이: PENDING/FAIL/HOLD -> HOLD 만 허용
    public void holdManually() {
        if (this.status == LotStatus.PASS) {
            throw new BusinessException(MaterialErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = LotStatus.HOLD;
    }

    // 수입검사 완료 시 자동 전이
    public void applyInspectionResult(LotStatus result) {
        this.status = result;
    }
}