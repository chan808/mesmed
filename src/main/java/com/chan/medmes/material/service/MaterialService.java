package com.chan.medmes.material.service;

import com.chan.medmes.global.error.BusinessException;
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

    // RawMaterial
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
        return rawMaterialRepository.findAll().stream()
                .map(RawMaterialResponse::from)
                .toList();
    }

    public RawMaterial findMaterialEntityById(Long id) {
        return rawMaterialRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.MATERIAL_NOT_FOUND));
    }

    // InspectionSpec
    @Transactional
    public InspectionSpecResponse createSpec(InspectionSpecRequest request) {
        RawMaterial material = findMaterialEntityById(request.rawMaterialId());
        InspectionSpec spec = InspectionSpec.builder()
                .rawMaterial(material).itemName(request.itemName())
                .specDesc(request.specDesc()).method(request.method())
                .equipment(request.equipment()).timing(request.timing())
                .build();
        return InspectionSpecResponse.from(inspectionSpecRepository.save(spec));
    }

    public List<InspectionSpecResponse> getSpecsByMaterial(Long materialId) {
        findMaterialEntityById(materialId);
        return inspectionSpecRepository.findByRawMaterialId(materialId).stream()
                .map(InspectionSpecResponse::from)
                .toList();
    }

    public InspectionSpec findSpecEntityById(Long id) {
        return inspectionSpecRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.SPEC_NOT_FOUND));
    }

    // Lot
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
        return lotRepository.findAll().stream()
                .map(LotResponse::from)
                .toList();
    }

    @Transactional
    public LotResponse updateLotStatus(Long id, LotStatusRequest request) {
        Lot lot = findLotEntityById(id);
        lot.updateStatus(request.status());
        return LotResponse.from(lot);
    }

    public Lot findLotEntityById(Long id) {
        return lotRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.LOT_NOT_FOUND));
    }

    private String generateLotNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "LOT-" + date + "-";
        long seq = lotRepository.countByLotNoStartingWith(prefix) + 1;
        return String.format("%s%03d", prefix, seq);
    }
}
