package com.chan.medmes.inspection.dto;

import com.chan.medmes.inspection.entity.InspectionDetail;
import com.chan.medmes.inspection.entity.InspectionRecord;
import com.chan.medmes.inspection.enums.InspectionResult;

import java.time.LocalDateTime;
import java.util.List;

public record InspectionResponse(
        Long id,
        Long lotId,
        String lotNo,
        String inspectorName,
        InspectionResult overallResult,
        String note,
        LocalDateTime inspectedAt,
        List<DetailResponse> details
) {
    public record DetailResponse(
            Long id,
            Long specId,
            String itemName,
            String specDesc,
            String measuredValue,
            InspectionResult result
    ) {
        public static DetailResponse from(InspectionDetail detail) {
            return new DetailResponse(
                    detail.getId(),
                    detail.getSpec().getId(),
                    detail.getSpec().getItemName(),
                    detail.getSpec().getSpecDesc(),
                    detail.getMeasuredValue(),
                    detail.getResult()
            );
        }
    }

    public static InspectionResponse from(InspectionRecord record, List<InspectionDetail> details) {
        return new InspectionResponse(
                record.getId(),
                record.getLot().getId(),
                record.getLot().getLotNo(),
                record.getInspector() != null ? record.getInspector().getDisplayName() : null,
                record.getOverallResult(),
                record.getNote(),
                record.getInspectedAt(),
                details.stream().map(DetailResponse::from).toList()
        );
    }
}
