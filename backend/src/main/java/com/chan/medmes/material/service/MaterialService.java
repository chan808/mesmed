package com.chan.medmes.material.service;

import com.chan.medmes.global.error.BusinessException;
import com.chan.medmes.material.enums.LotStatus;
import com.chan.medmes.material.MaterialErrorCode;
import com.chan.medmes.material.dto.*;
import com.chan.medmes.material.entity.InspectionSpec;
import com.chan.medmes.material.entity.Lot;
import com.chan.medmes.material.entity.RawMaterial;
import com.chan.medmes.material.repository.InspectionSpecRepository;
import com.chan.medmes.material.repository.LotRepository;
import com.chan.medmes.material.repository.RawMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialService {

    private final RawMaterialRepository rawMaterialRepository;
    private final InspectionSpecRepository inspectionSpecRepository;
    private final LotRepository lotRepository;

    // ── RawMaterial ───────────────────────────────────────────────

    // 신규 자재(원자재 등)를 생성하며, 코드 중복 여부를 검증합니다.
    @Transactional
    public RawMaterialResponse createMaterial(RawMaterialRequest request) {
        if (rawMaterialRepository.existsByCode(request.code())) {
            throw new BusinessException(MaterialErrorCode.MATERIAL_CODE_DUPLICATED);
        }
        RawMaterial material = RawMaterial.builder()
                .code(request.code()).name(request.name())
                .category(request.category()).unit(request.unit())
                .specStandard(request.specStandard())
                .build();
        return RawMaterialResponse.from(rawMaterialRepository.save(material));
    }

    public RawMaterialResponse getMaterial(Long id) {
        return RawMaterialResponse.from(findMaterialEntityById(id));
    }

    public List<RawMaterialResponse> getAllMaterials() {
        return rawMaterialRepository.findAllByDeletedAtIsNull().stream()
                .map(RawMaterialResponse::from)
                .toList();
    }

    // 자재 정보를 Soft Delete 방식으로 삭제하며, 이미 삭제된 경우 예외를 발생시킵니다.
    @Transactional
    public void deleteMaterial(Long id) {
        RawMaterial material = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.MATERIAL_NOT_FOUND));
        if (material.getDeletedAt() != null) {
            throw new BusinessException(MaterialErrorCode.MATERIAL_ALREADY_DELETED);
        }
        material.softDelete();
    }

    public RawMaterial findMaterialEntityById(Long id) {
        return rawMaterialRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.MATERIAL_NOT_FOUND));
    }

    // ── InspectionSpec ────────────────────────────────────────────

    // 특정 자재에 대한 신규 검사 스펙을 등록합니다.
    @Transactional
    public InspectionSpecResponse createSpec(InspectionSpecRequest request) {
        RawMaterial material = findMaterialEntityById(request.rawMaterialId());
        InspectionSpec spec = InspectionSpec.builder()
                .rawMaterial(material)
                .category(request.category())
                .itemName(request.itemName())
                .specDesc(request.specDesc())
                .method(request.method())
                .methodCustom(request.methodCustom())
                .equipment(request.equipment())
                .equipmentCustom(request.equipmentCustom())
                .timing(request.timing())
                .timingCustom(request.timingCustom())
                .measureType(request.measureType())
                .minValue(request.minValue()).maxValue(request.maxValue())
                .unit(request.unit())
                .build();
        return InspectionSpecResponse.from(inspectionSpecRepository.save(spec));
    }

    public List<InspectionSpecResponse> getSpecsByMaterial(Long materialId) {
        findMaterialEntityById(materialId);
        return inspectionSpecRepository.findByRawMaterialIdAndSupersededAtIsNull(materialId).stream()
                .map(InspectionSpecResponse::from)
                .toList();
    }

    public InspectionSpec findSpecEntityById(Long id) {
        // 이력 조회 시 구버전 spec도 로드 가능해야 하므로 삭제 필터 없음
        return inspectionSpecRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.SPEC_NOT_FOUND));
    }

    // 기존 스펙을 무효화(Supersede)하고, 버전을 올려 새로운 스펙으로 갱신합니다.
    @Transactional
    public InspectionSpecResponse updateSpec(Long id, InspectionSpecRequest request) {
        InspectionSpec current = inspectionSpecRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.SPEC_NOT_FOUND));
        if (current.getSupersededAt() != null) {
            throw new BusinessException(MaterialErrorCode.SPEC_ALREADY_SUPERSEDED);
        }
        current.supersede();

        InspectionSpec next = InspectionSpec.builder()
                .rawMaterial(current.getRawMaterial())
                .category(request.category())
                .itemName(request.itemName())
                .specDesc(request.specDesc())
                .method(request.method())
                .methodCustom(request.methodCustom())
                .equipment(request.equipment())
                .equipmentCustom(request.equipmentCustom())
                .timing(request.timing())
                .timingCustom(request.timingCustom())
                .measureType(request.measureType())
                .minValue(request.minValue()).maxValue(request.maxValue())
                .unit(request.unit())
                .version(current.getVersion() + 1)
                .build();
        return InspectionSpecResponse.from(inspectionSpecRepository.save(next));
    }

    // 검사 스펙을 논리적으로 무효화(Supersede) 처리하여 삭제를 대신합니다.
    @Transactional
    public void deleteSpec(Long id) {
        InspectionSpec spec = inspectionSpecRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.SPEC_NOT_FOUND));
        if (spec.getSupersededAt() != null) {
            throw new BusinessException(MaterialErrorCode.SPEC_ALREADY_SUPERSEDED);
        }
        spec.supersede();
    }

    // ── Lot ───────────────────────────────────────────────────────

    // 자재 입고 시 새로운 Lot를 생성하며, Lot 번호가 없으면 자동 생성합니다.
    @Transactional
    public LotResponse createLot(LotRequest request) {
        RawMaterial material = findMaterialEntityById(request.rawMaterialId());
        String lotNo = (request.lotNo() != null) ? request.lotNo() : generateLotNo();

        if (lotRepository.existsByLotNo(lotNo)) {
            throw new BusinessException(MaterialErrorCode.LOT_NO_DUPLICATED);
        }
        Lot lot = Lot.builder()
                .lotNo(lotNo).rawMaterial(material)
                .quantity(request.quantity())
                .receivedAt(request.receivedAt())
                .supplier(request.supplier())
                .build();
        return LotResponse.from(lotRepository.save(lot));
    }

    public LotResponse getLot(Long id) {
        return LotResponse.from(findLotEntityById(id));
    }

    public List<LotResponse> getAllLots() {
        return lotRepository.findAllByDeletedAtIsNull().stream()
                .map(LotResponse::from)
                .toList();
    }

    // Lot의 상태를 수동으로 보류(HOLD) 상태로 변경하며 상태 전환 가능 여부를 검증합니다.
    @Transactional
    public LotResponse updateLotStatus(Long id, LotStatusRequest request) {
        Lot lot = findLotEntityById(id);
        if (request.status() != LotStatus.HOLD) {
            throw new BusinessException(MaterialErrorCode.INVALID_STATUS_TRANSITION);
        }
        lot.holdManually();
        return LotResponse.from(lot);
    }

    // Lot 정보를 Soft Delete 방식으로 삭제 처리합니다.
    @Transactional
    public void deleteLot(Long id) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.LOT_NOT_FOUND));
        if (lot.getDeletedAt() != null) {
            throw new BusinessException(MaterialErrorCode.LOT_ALREADY_DELETED);
        }
        lot.softDelete();
    }

    public Lot findLotEntityById(Long id) {
        return lotRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.LOT_NOT_FOUND));
    }

    // 'LOT-yyyyMMdd-일련번호' 형식으로 새로운 Lot 번호를 자동 생성합니다.
    private String generateLotNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "LOT-" + date + "-";
        long seq = lotRepository.countByLotNoStartingWith(prefix) + 1;
        return String.format("%s%03d", prefix, seq);
    }
}