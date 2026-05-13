package com.chan.medmes.production.repository;

import com.chan.medmes.production.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    boolean existsByEquipmentCode(String equipmentCode);
}