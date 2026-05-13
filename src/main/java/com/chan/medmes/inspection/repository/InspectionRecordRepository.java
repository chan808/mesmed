package com.chan.medmes.inspection.repository;

import com.chan.medmes.inspection.entity.InspectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionRecordRepository extends JpaRepository<InspectionRecord, Long> {
    List<InspectionRecord> findByLotId(Long lotId);
}
