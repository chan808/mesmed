package com.chan.medmes.material.repository;

import com.chan.medmes.material.entity.InspectionSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionSpecRepository extends JpaRepository<InspectionSpec, Long> {
    // 현행(개정되지 않은) 기준만 조회
    List<InspectionSpec> findByRawMaterialIdAndSupersededAtIsNull(Long rawMaterialId);
    boolean existsByRawMaterialId(Long rawMaterialId);
}