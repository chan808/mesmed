package com.chan.medmes.production.entity;

import com.chan.medmes.production.enums.EquipmentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_code", unique = true, nullable = false, length = 50)
    private String equipmentCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EquipmentStatus status;

    @Column(name = "last_maintained_at")
    private LocalDateTime lastMaintainedAt;

    @Builder
    public Equipment(String equipmentCode, String name) {
        this.equipmentCode = equipmentCode;
        this.name = name;
        this.status = EquipmentStatus.IDLE;
    }

    public void updateStatus(EquipmentStatus status) {
        this.status = status;
    }

    public void recordMaintenance() {
        this.lastMaintainedAt = LocalDateTime.now();
        this.status = EquipmentStatus.IDLE;
    }
}
