package com.chan.medmes.inspection.repository;

import com.chan.medmes.inspection.entity.VisionInspection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisionInspectionRepository extends JpaRepository<VisionInspection, Long> {
    List<VisionInspection> findByLotId(Long lotId);
    Optional<VisionInspection> findTopByLotIdOrderByInspectedAtDesc(Long lotId);
}