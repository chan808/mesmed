package com.chan.medmes.material.service;

import com.chan.medmes.global.error.BusinessException;
import com.chan.medmes.material.enums.LotStatus;
import com.chan.medmes.material.dto.LotRequest;
import com.chan.medmes.material.dto.LotResponse;
import com.chan.medmes.material.dto.LotStatusRequest;
import com.chan.medmes.material.entity.Lot;
import com.chan.medmes.material.entity.RawMaterial;
import com.chan.medmes.material.repository.InspectionSpecRepository;
import com.chan.medmes.material.repository.LotRepository;
import com.chan.medmes.material.repository.RawMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @InjectMocks MaterialService materialService;
    @Mock RawMaterialRepository rawMaterialRepository;
    @Mock InspectionSpecRepository inspectionSpecRepository;
    @Mock LotRepository lotRepository;

    private RawMaterial material;

    @BeforeEach
    void setUp() {
        material = RawMaterial.builder().code("MAT-001").name("전극").build();
    }

    @Test
    void 중복_LOT번호_등록시_예외가_발생한다() {
        LotRequest request = new LotRequest(1L, "LOT-DUP-001", 50, null, null);
        when(rawMaterialRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(material));
        when(lotRepository.existsByLotNo("LOT-DUP-001")).thenReturn(true);

        assertThatThrownBy(() -> materialService.createLot(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void LOT번호가_null이면_LOT_접두사로_자동생성된다() {
        LotRequest request = new LotRequest(1L, null, 50, null, null);
        Lot saved = Lot.builder()
                .lotNo("LOT-20260514-001").rawMaterial(material).quantity(50).build();

        when(rawMaterialRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(material));
        when(lotRepository.countByLotNoStartingWith(anyString())).thenReturn(0L);
        when(lotRepository.existsByLotNo(anyString())).thenReturn(false);
        when(lotRepository.save(any())).thenReturn(saved);

        LotResponse response = materialService.createLot(request);

        assertThat(response.lotNo()).startsWith("LOT-");
    }

    @Test
    void PASS된_LOT는_HOLD로_변경할_수_없다() {
        Lot passedLot = Lot.builder()
                .lotNo("LOT-20260514-001").rawMaterial(material).quantity(50).build();
        passedLot.applyInspectionResult(LotStatus.PASS);

        when(lotRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(passedLot));

        assertThatThrownBy(() -> materialService.updateLotStatus(1L, new LotStatusRequest(LotStatus.HOLD)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void HOLD_이외의_상태로_수동변경_요청시_예외가_발생한다() {
        Lot pendingLot = Lot.builder()
                .lotNo("LOT-20260514-002").rawMaterial(material).quantity(30).build();

        when(lotRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(pendingLot));

        assertThatThrownBy(() -> materialService.updateLotStatus(1L, new LotStatusRequest(LotStatus.PASS)))
                .isInstanceOf(BusinessException.class);
    }
}
