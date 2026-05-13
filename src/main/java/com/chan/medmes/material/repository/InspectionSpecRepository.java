package com.chan.medmes.material.repository;

import com.chan.medmes.material.entity.InspectionSpec;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionSpecRepository extends JpaRepository<InspectionSpec, Long> {
    List<InspectionSpec> findByRawMaterialId(Long rawMaterialId);
    boolean existsByRawMaterialId(Long rawMaterialId);
}
