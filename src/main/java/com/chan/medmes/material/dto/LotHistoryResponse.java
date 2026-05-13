package com.chan.medmes.material.dto;

import com.chan.medmes.material.LotStatus;

import java.time.LocalDateTime;
import java.util.List;

public record LotHistoryResponse(
        LotInfo lot,
        InspectionRecordInfo inspectionRecord,
        List<ProductionLogInfo> productionLogs
) {
    public record LotInfo(
            String lotNo,
            String rawMaterialCode,
            String rawMaterialName,
            Integer quantity,
            LocalDateTime receivedAt,
            String supplier,
            LotStatus status
    ) {}

    public record InspectionRecordInfo(
            Long recordId,
            String overallResult,
            LocalDateTime inspectedAt,
            String inspectorName,
            String note,
            List<DetailInfo> details
    ) {}

    public record DetailInfo(
            String itemName,
            String specDesc,
            String measuredValue,
            String result
    ) {}

    public record ProductionLogInfo(
            Long logId,
            String processName,
            String equipmentName,
            Integer producedQty,
            Integer defectQty,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {}
}
