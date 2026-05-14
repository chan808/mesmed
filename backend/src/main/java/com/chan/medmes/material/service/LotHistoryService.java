package com.chan.medmes.material.service;

import com.chan.medmes.global.error.BusinessException;
import com.chan.medmes.inspection.entity.InspectionDetail;
import com.chan.medmes.inspection.entity.InspectionRecord;
import com.chan.medmes.inspection.repository.InspectionDetailRepository;
import com.chan.medmes.inspection.repository.InspectionRecordRepository;
import com.chan.medmes.material.MaterialErrorCode;
import com.chan.medmes.material.dto.LotHistoryResponse;
import com.chan.medmes.material.entity.Lot;
import com.chan.medmes.material.repository.LotRepository;
import com.chan.medmes.production.entity.ProductionLog;
import com.chan.medmes.production.repository.ProductionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotHistoryService {

    private final LotRepository lotRepository;
    private final InspectionRecordRepository inspectionRecordRepository;
    private final InspectionDetailRepository inspectionDetailRepository;
    private final ProductionLogRepository productionLogRepository;

    public LotHistoryResponse getHistory(Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.LOT_NOT_FOUND));

        LotHistoryResponse.LotInfo lotInfo = new LotHistoryResponse.LotInfo(
                lot.getLotNo(),
                lot.getRawMaterial().getCode(),
                lot.getRawMaterial().getName(),
                lot.getQuantity(),
                lot.getReceivedAt(),
                lot.getSupplier(),
                lot.getStatus()
        );

        List<InspectionRecord> records = inspectionRecordRepository.findByLotId(lotId);

        // record ID 목록으로 세부 항목 일괄 조회 (N+1 방지)
        List<Long> recordIds = records.stream().map(InspectionRecord::getId).toList();
        Map<Long, List<InspectionDetail>> detailMap = inspectionDetailRepository
                .findByRecord_IdIn(recordIds).stream()
                .collect(Collectors.groupingBy(d -> d.getRecord().getId()));

        List<LotHistoryResponse.InspectionRecordInfo> inspectionInfos = records.stream()
                .map(r -> toInspectionRecordInfo(r, detailMap.getOrDefault(r.getId(), List.of())))
                .toList();

        List<LotHistoryResponse.ProductionLogInfo> productionInfos =
                productionLogRepository.findByLotId(lotId).stream()
                        .map(this::toProductionLogInfo)
                        .toList();

        return new LotHistoryResponse(lotInfo, inspectionInfos, productionInfos);
    }

    private LotHistoryResponse.InspectionRecordInfo toInspectionRecordInfo(
            InspectionRecord record, List<InspectionDetail> details) {
        List<LotHistoryResponse.DetailInfo> detailInfos = details.stream()
                .map(this::toDetailInfo)
                .toList();

        return new LotHistoryResponse.InspectionRecordInfo(
                record.getId(),
                record.getOverallResult() != null ? record.getOverallResult().name() : null,
                record.getInspectedAt(),
                record.getInspector() != null ? record.getInspector().getDisplayName() : null,
                record.getNote(),
                detailInfos
        );
    }

    private LotHistoryResponse.DetailInfo toDetailInfo(InspectionDetail detail) {
        return new LotHistoryResponse.DetailInfo(
                detail.getSpec().getItemName(),
                detail.getSpec().getSpecDesc(),
                detail.getMeasuredValue(),
                detail.getResult().name(),
                detail.getSeverity() != null ? detail.getSeverity().name() : null
        );
    }

    private LotHistoryResponse.ProductionLogInfo toProductionLogInfo(ProductionLog log) {
        return new LotHistoryResponse.ProductionLogInfo(
                log.getId(),
                log.getProcessName(),
                log.getEquipment() != null ? log.getEquipment().getName() : null,
                log.getProducedQty(),
                log.getDefectQty(),
                log.getStartedAt(),
                log.getEndedAt()
        );
    }
}
