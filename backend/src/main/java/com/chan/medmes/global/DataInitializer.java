package com.chan.medmes.global;

import com.chan.medmes.material.entity.InspectionSpec;
import com.chan.medmes.material.entity.RawMaterial;
import com.chan.medmes.material.enums.*;
import com.chan.medmes.material.repository.InspectionSpecRepository;
import com.chan.medmes.material.repository.RawMaterialRepository;
import com.chan.medmes.production.entity.Equipment;
import com.chan.medmes.production.repository.EquipmentRepository;
import com.chan.medmes.user.enums.UserRole;
import com.chan.medmes.user.entity.User;
import com.chan.medmes.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RawMaterialRepository rawMaterialRepository;
    private final InspectionSpecRepository inspectionSpecRepository;
    private final EquipmentRepository equipmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        seedMaterialsAndSpecs();
        seedEquipment();
    }

    // ── 사용자 ────────────────────────────────────────────────────

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .displayName("관리자")
                .role(UserRole.ADMIN)
                .build());

        userRepository.save(User.builder()
                .username("inspector")
                .password(passwordEncoder.encode("inspector123"))
                .displayName("검사담당자")
                .role(UserRole.INSPECTOR)
                .build());

        userRepository.save(User.builder()
                .username("operator")
                .password(passwordEncoder.encode("operator123"))
                .displayName("생산작업자")
                .role(UserRole.OPERATOR)
                .build());

        log.info("================================================");
        log.info("초기 계정 생성 완료");
        log.info("  admin      / admin123");
        log.info("  inspector  / inspector123");
        log.info("  operator   / operator123");
        log.info("================================================");
    }

    // ── 원자재 6종 + 검사기준 ─────────────────────────────────────

    private void seedMaterialsAndSpecs() {
        if (rawMaterialRepository.count() > 0) return;

        seedPetPatchA();
        seedPetPatchB();
        seedMembrane();
        seedMainBoard();
        seedBattery();
        seedLcd();

        log.info("원자재 6종 + 검사기준 시드 데이터 생성 완료");
    }

    // 펫패치A — 실리콘 패드 (30mm)
    private void seedPetPatchA() {
        RawMaterial m = rawMaterialRepository.save(RawMaterial.builder()
                .code("RM-001")
                .name("펫패치A")
                .category("실리콘 부품")
                .unit("개")
                .specStandard("실리콘 패드 30mm")
                .build());

        // 치수: NUMERIC, 30 ± 1mm
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.DIMENSION)
                .itemName("치수")
                .specDesc("30mm ± 1mm")
                .method(InspectionMethod.MEASURE)
                .equipment(InspectionEquipment.CALIPER)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.NUMERIC)
                .minValue(29.0).maxValue(31.0).unit("mm")
                .build());

        // 미성형
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("미성형")
                .specDesc("미성형 부분이 없을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());

        // 오염
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("오염")
                .specDesc("흑점 등의 오염이 없을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());

        // 이상 유/무
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("이상 유/무")
                .specDesc("힘 등의 이상이 없을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());
    }

    // 펫패치B — 전극봉 (35mm)
    private void seedPetPatchB() {
        RawMaterial m = rawMaterialRepository.save(RawMaterial.builder()
                .code("RM-002")
                .name("펫패치B")
                .category("전극 부품")
                .unit("개")
                .specStandard("전극봉 35mm")
                .build());

        // 치수: NUMERIC, 35 ± 1mm
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.DIMENSION)
                .itemName("치수")
                .specDesc("35mm ± 1mm")
                .method(InspectionMethod.MEASURE)
                .equipment(InspectionEquipment.CALIPER)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.NUMERIC)
                .minValue(34.0).maxValue(36.0).unit("mm")
                .build());

        // 오염
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("오염")
                .specDesc("흑점 등의 오염이 없을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());

        // 이상 유/무
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("이상 유/무")
                .specDesc("찢김 등의 이상이 없을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());
    }

    // 멤브레인 — 포장재 (200×300mm)
    private void seedMembrane() {
        RawMaterial m = rawMaterialRepository.save(RawMaterial.builder()
                .code("RM-003")
                .name("멤브레인")
                .category("포장재")
                .unit("개")
                .specStandard("포장재 200×300mm")
                .build());

        // 가로 치수: NUMERIC, 200 ± 1mm
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.DIMENSION)
                .itemName("가로 치수")
                .specDesc("200mm ± 1mm")
                .method(InspectionMethod.MEASURE)
                .equipment(InspectionEquipment.CALIPER)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.NUMERIC)
                .minValue(199.0).maxValue(201.0).unit("mm")
                .build());

        // 세로 치수: NUMERIC, 300 ± 1mm
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.DIMENSION)
                .itemName("세로 치수")
                .specDesc("300mm ± 1mm")
                .method(InspectionMethod.MEASURE)
                .equipment(InspectionEquipment.CALIPER)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.NUMERIC)
                .minValue(299.0).maxValue(301.0).unit("mm")
                .build());

        // 오염
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("오염")
                .specDesc("오염이 없을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());

        // 이상 유/무
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("이상 유/무")
                .specDesc("찢김 혹은 변색이 없을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());
    }

    // Main Board — 심전계 메인보드
    private void seedMainBoard() {
        RawMaterial m = rawMaterialRepository.save(RawMaterial.builder()
                .code("RM-004")
                .name("Main Board")
                .category("전자 부품")
                .unit("개")
                .specStandard("심전계 메인보드 PCB")
                .build());

        // 이상 유/무
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("이상 유/무")
                .specDesc("깨짐, 휨 등의 이상이 없을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());

        // 납땜
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("납땜")
                .specDesc("납땜이 정상적으로 되었을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());

        // ECG 성능
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.PERFORMANCE)
                .itemName("ECG 성능")
                .specDesc("ECG 신호에 정상적으로 반응할 것")
                .method(InspectionMethod.TEST)
                .equipment(InspectionEquipment.ECG_SIMULATOR)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());

        // 정상작동
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.PERFORMANCE)
                .itemName("정상작동")
                .specDesc("부팅 및 블루투스 등 기능이 정상적으로 작동할 것")
                .method(InspectionMethod.TEST)
                .equipment(InspectionEquipment.HOST_DEVICE)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());
    }

    // 배터리
    private void seedBattery() {
        RawMaterial m = rawMaterialRepository.save(RawMaterial.builder()
                .code("RM-005")
                .name("배터리")
                .category("전기 부품")
                .unit("개")
                .specStandard("리튬이온 배터리 3.7V")
                .build());

        // 전압: NUMERIC, 3.7~4.2V, 전수 측정
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.ELECTRICAL)
                .itemName("전압")
                .specDesc("충전전압(3.7V~4.2V) 일치할 것")
                .method(InspectionMethod.MEASURE)
                .equipment(InspectionEquipment.MULTIMETER)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.NUMERIC)
                .minValue(3.7).maxValue(4.2).unit("V")
                .build());

        // 충/방전: VISUAL, 샘플 측정
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.PERFORMANCE)
                .itemName("충/방전")
                .specDesc("충전(2H~3H) 및 방전시간(최대24H) 확인할 것")
                .method(InspectionMethod.MEASURE)
                .equipment(InspectionEquipment.STOPWATCH)
                .timing(InspectionTiming.SAMPLE)
                .measureType(MeasureType.VISUAL)
                .build());
    }

    // LCD
    private void seedLcd() {
        RawMaterial m = rawMaterialRepository.save(RawMaterial.builder()
                .code("RM-006")
                .name("LCD")
                .category("디스플레이 부품")
                .unit("개")
                .specStandard("LCD 모듈")
                .build());

        // DOT 깨짐
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("DOT 깨짐")
                .specDesc("DOT 깨짐이 0일 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());

        // FILM CABLE
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("FILM CABLE")
                .specDesc("접촉 커넥터 부위에 이물질이 없을 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());

        // BLACK LIGHT
        inspectionSpecRepository.save(InspectionSpec.builder()
                .rawMaterial(m)
                .category(InspectionCategory.APPEARANCE)
                .itemName("BLACK LIGHT")
                .specDesc("밝기가 균일할 것")
                .method(InspectionMethod.VISUAL_CHECK)
                .equipment(InspectionEquipment.VISUAL_INSPECTION)
                .timing(InspectionTiming.FULL)
                .measureType(MeasureType.VISUAL)
                .build());
    }

    // ── 설비 ──────────────────────────────────────────────────────

    private void seedEquipment() {
        if (equipmentRepository.count() > 0) return;

        equipmentRepository.save(Equipment.builder()
                .equipmentCode("EQ-COAT-001")
                .name("점착제 코팅기")
                .build());

        equipmentRepository.save(Equipment.builder()
                .equipmentCode("EQ-SMT-001")
                .name("SMT 설비")
                .build());

        equipmentRepository.save(Equipment.builder()
                .equipmentCode("EQ-ASSY-001")
                .name("조립 설비")
                .build());

        equipmentRepository.save(Equipment.builder()
                .equipmentCode("EQ-PACK-001")
                .name("포장기")
                .build());

        log.info("설비 4종 시드 데이터 생성 완료");
    }
}