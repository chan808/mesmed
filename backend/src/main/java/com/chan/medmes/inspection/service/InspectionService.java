package com.chan.medmes.inspection.service;

import com.chan.medmes.global.error.BusinessException;
import com.chan.medmes.inspection.dto.InspectionDetailItem;
import com.chan.medmes.inspection.dto.InspectionRequest;
import com.chan.medmes.inspection.dto.InspectionResponse;
import com.chan.medmes.inspection.entity.InspectionDetail;
import com.chan.medmes.inspection.entity.InspectionRecord;
import com.chan.medmes.inspection.enums.InspectionResult;
import com.chan.medmes.inspection.error.InspectionErrorCode;
import com.chan.medmes.inspection.repository.InspectionDetailRepository;
import com.chan.medmes.inspection.repository.InspectionRecordRepository;
import com.chan.medmes.material.enums.LotStatus;
import com.chan.medmes.material.enums.MeasureType;
import com.chan.medmes.material.entity.InspectionSpec;
import com.chan.medmes.material.entity.Lot;
import com.chan.medmes.material.service.MaterialService;
import com.chan.medmes.user.entity.User;
import com.chan.medmes.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InspectionService {

    private final InspectionRecordRepository recordRepository;
    private final InspectionDetailRepository detailRepository;
    private final MaterialService materialService;
    private final UserService userService;

    @Transactional
    public InspectionResponse createInspection(InspectionRequest request) {
        Lot lot = materialService.findLotEntityById(request.lotId());

        User inspector = (request.inspectorId() != null)
                ? userService.findEntityById(request.inspectorId())
                : null;

        InspectionRecord record = InspectionRecord.builder()
                .lot(lot)
                .inspector(inspector)
                .note(request.note())
                .build();
        recordRepository.save(record);

        List<InspectionDetail> details = request.details().stream()
                .map(item -> buildDetail(record, item))
                .toList();
        detailRepository.saveAll(details);

        boolean anyFail = details.stream()
                .anyMatch(d -> d.getResult() == InspectionResult.FAIL);
        InspectionResult overall = anyFail ? InspectionResult.FAIL : InspectionResult.PASS;

        record.conclude(overall);
        lot.applyInspectionResult(anyFail ? LotStatus.FAIL : LotStatus.PASS);

        return InspectionResponse.from(record, details);
    }

    public InspectionResponse getInspection(Long id) {
        InspectionRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(InspectionErrorCode.INSPECTION_NOT_FOUND));
        List<InspectionDetail> details = detailRepository.findByRecord_Id(id);
        return InspectionResponse.from(record, details);
    }

    public List<InspectionResponse> getInspectionsByLot(Long lotId) {
        return recordRepository.findByLotId(lotId).stream()
                .map(record -> InspectionResponse.from(
                        record, detailRepository.findByRecord_Id(record.getId())))
                .toList();
    }

    private InspectionDetail buildDetail(InspectionRecord record, InspectionDetailItem item) {
        InspectionSpec spec = materialService.findSpecEntityById(item.inspectionSpecId());

        Long lotMaterialId = record.getLot().getRawMaterial().getId();
        if (!spec.getRawMaterial().getId().equals(lotMaterialId)) {
            throw new BusinessException(InspectionErrorCode.SPEC_MATERIAL_MISMATCH);
        }

        InspectionResult result = (spec.getMeasureType() == MeasureType.NUMERIC)
                ? judgeNumeric(spec, item.measuredValue())
                : item.result();

        return InspectionDetail.builder()
                .record(record)
                .spec(spec)
                .measuredValue(item.measuredValue())
                .result(result)
                .severity(item.severity())
                .build();
    }

    // NUMERIC 기준으로 측정값이 [minValue, maxValue] 범위 안이면 PASS
    private InspectionResult judgeNumeric(InspectionSpec spec, String measuredValue) {
        if (measuredValue == null) {
            throw new BusinessException(InspectionErrorCode.MEASURED_VALUE_REQUIRED);
        }
        try {
            double value = Double.parseDouble(measuredValue);
            boolean inRange = (spec.getMinValue() == null || value >= spec.getMinValue())
                    && (spec.getMaxValue() == null || value <= spec.getMaxValue());
            return inRange ? InspectionResult.PASS : InspectionResult.FAIL;
        } catch (NumberFormatException e) {
            throw new BusinessException(InspectionErrorCode.MEASURED_VALUE_NOT_NUMERIC);
        }
    }
}
