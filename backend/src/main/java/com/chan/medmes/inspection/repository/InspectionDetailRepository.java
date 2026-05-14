package com.chan.medmes.inspection.repository;

import com.chan.medmes.inspection.entity.InspectionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionDetailRepository extends JpaRepository<InspectionDetail, Long> {
    List<InspectionDetail> findByRecord_Id(Long recordId);
    List<InspectionDetail> findByRecord_IdIn(List<Long> recordIds);
}
