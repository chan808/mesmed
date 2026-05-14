package com.chan.medmes.inspection.service;

import com.chan.medmes.global.error.BusinessException;
import com.chan.medmes.inspection.dto.InspectionDetailItem;
import com.chan.medmes.inspection.dto.InspectionRequest;
import com.chan.medmes.inspection.enums.InspectionResult;
import com.chan.medmes.inspection.enums.InspectionSeverity;
import com.chan.medmes.inspection.repository.InspectionDetailRepository;
import com.chan.medmes.inspection.repository.InspectionRecordRepository;
import com.chan.medmes.material.enums.LotStatus;
import com.chan.medmes.material.entity.InspectionSpec;
import com.chan.medmes.material.entity.Lot;
import com.chan.medmes.material.entity.RawMaterial;
import com.chan.medmes.material.service.MaterialService;
import com.chan.medmes.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @InjectMocks InspectionService inspectionService;
    @Mock InspectionRecordRepository recordRepository;
    @Mock InspectionDetailRepository detailRepository;
    @Mock MaterialService materialService;
    @Mock UserService userService;

    private RawMaterial material;
    private InspectionSpec spec;
    private Lot lot;

    @BeforeEach
    void setUp() {
        material = RawMaterial.builder().code("MAT-001").name("전극").build();
        ReflectionTestUtils.setField(material, "id", 1L);

        spec = InspectionSpec.builder().rawMaterial(material).itemName("외관검사").build();

        lot = Lot.builder().lotNo("LOT-20260514-001").rawMaterial(material).quantity(50).build();

        when(materialService.findLotEntityById(1L)).thenReturn(lot);
        when(recordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void 모든_항목_PASS이면_LOT상태가_PASS가_된다() {
        InspectionRequest request = new InspectionRequest(
                1L, null, "이상없음",
                List.of(new InspectionDetailItem(1L, "12mm", InspectionResult.PASS, InspectionSeverity.MINOR))
        );
        when(materialService.findSpecEntityById(1L)).thenReturn(spec);
        when(detailRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        inspectionService.createInspection(request);

        assertThat(lot.getStatus()).isEqualTo(LotStatus.PASS);
    }

    @Test
    void 항목_중_하나라도_FAIL이면_LOT상태가_FAIL이_된다() {
        InspectionRequest request = new InspectionRequest(
                1L, null, null,
                List.of(
                        new InspectionDetailItem(1L, "정상", InspectionResult.PASS, InspectionSeverity.MINOR),
                        new InspectionDetailItem(1L, "불량", InspectionResult.FAIL, InspectionSeverity.CRITICAL)
                )
        );
        when(materialService.findSpecEntityById(1L)).thenReturn(spec);
        when(detailRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        inspectionService.createInspection(request);

        assertThat(lot.getStatus()).isEqualTo(LotStatus.FAIL);
    }

    @Test
    void 다른_원자재의_검사기준_사용시_예외가_발생한다() {
        RawMaterial other = RawMaterial.builder().code("MAT-999").name("다른원자재").build();
        ReflectionTestUtils.setField(other, "id", 999L);
        InspectionSpec wrongSpec = InspectionSpec.builder().rawMaterial(other).itemName("틀린기준").build();

        InspectionRequest request = new InspectionRequest(
                1L, null, null,
                List.of(new InspectionDetailItem(99L, "측정값", InspectionResult.PASS, InspectionSeverity.MINOR))
        );
        when(materialService.findSpecEntityById(99L)).thenReturn(wrongSpec);

        assertThatThrownBy(() -> inspectionService.createInspection(request))
                .isInstanceOf(BusinessException.class);
    }
}
