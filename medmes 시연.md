# MedMES 프로젝트 시연 가이드

> 선임 개발자 대상 · 도메인별 흐름 + 기술적 설계 근거

---

## 한 줄 소개

**"의료기기 원자재의 수기 검사 기록을 DB로 전환하고,
LOT 번호 하나로 입고 → 검사 → 생산 전 과정을 추적하는 Spring Boot 백엔드입니다."**

---

## 왜 만들었나 — 해결한 문제

중소 의료기기 제조사는 원자재 수입검사를 종이 검사표로 운영합니다.
세 가지 문제가 있습니다.

1. **감사 대응 지연**: 식약처 GMP 감사 시 특정 LOT 이력을 수기 문서에서 찾는 데 수 시간이 걸린다.
2. **불량 투입 위험**: 검사 실패한 원자재가 담당자 실수로 생산 라인에 들어갈 수 있다.
3. **데이터 단절**: 입고·검사·생산이 각각 다른 문서에 기록돼 LOT 하나의 전체 이력을 한눈에 볼 수 없다.

이 세 가지를 해결하는 것이 이 시스템의 목적입니다.

---

## 기술 스택 요약

| 영역 | 선택 | 이유 |
|---|---|---|
| Runtime | Java 17 + Spring Boot 4.x | 엔터프라이즈 표준, Record 타입 활용 |
| DB | PostgreSQL | ACID 보장, JSONB 등 확장성 |
| ORM | Spring Data JPA | 도메인 중심 설계에 적합 |
| Auth | Spring Security + JWT (jjwt 0.12.x) | Stateless, 멀티 클라이언트 대응 |
| 문서 | springdoc-openapi | Swagger UI 자동 생성 |
| 프론트 | React + Vite + TypeScript | API 연동 확인용 |

**핵심 설계 선택**: Request/Response를 Java **Record**로 통일.
불변·간결하고 Bean Validation을 필드에 직접 선언할 수 있어 가독성이 좋습니다.

---

## 패키지 구조 — Package by Feature

```
com.chan.medmes
├── global/          공통 인프라 (ApiResponse, ErrorCode, JWT, Security)
├── auth/            로그인, JWT 발급
├── user/            사용자 CRUD, BCrypt 비밀번호
├── material/        원자재 마스터, LOT 입고, 검사기준 (버전 관리)
├── inspection/      수입검사 등록, 자동 판정, LOT 상태 전이  ← 핵심 도메인
└── production/      설비 관리, 생산 이력, 알람, 대시보드
```

**패키지 간 의존 방향 (단방향 강제)**

```
global ← user ← auth
global ← material
global ← inspection → material, user
global ← production → material
material(LotHistoryService) → inspection, production  (읽기 전용 집계만 허용)
```

> **규칙**: Service는 다른 패키지의 Repository를 직접 주입하지 않습니다.
> 반드시 해당 패키지의 Service를 통해야 합니다.
> 이 규칙 덕분에 LOT 조회 시 soft delete 필터 같은 비즈니스 규칙이 한 곳에서만 관리됩니다.

---

## 전체 흐름 요약

```
원자재 등록 → 검사기준 등록 → LOT 입고 → 수입검사 등록
                                               ↓
                                  자동 판정: PASS / FAIL
                                               ↓
                              PASS이면 생산 투입 가능 / FAIL이면 차단
                                               ↓
                                  LOT 이력 단일 API로 전체 조회
```

---

## 도메인 1 — `material` : 원자재 마스터 + LOT + 검사기준

### 무엇을 관리하나

- **RawMaterial**: 원자재 마스터 (코드, 이름, 분류, 단위, 대표 규격)
- **InspectionSpec**: 원자재별 검사기준 (검사항목, 판정 방식, 측정 기기)
- **Lot**: 원자재 입고 단위 (고유 LOT 번호, 수량, 공급사, 상태)

### 설계 포인트

**① Soft Delete**

원자재나 LOT를 실제로 삭제하지 않고 `deleted_at` 타임스탬프만 기록합니다.
`IS_ACTIVE boolean` 대신 타임스탬프를 쓴 이유는 "언제 삭제됐는지"까지 기록해야
의료기기 GMP 감사 대응이 가능하기 때문입니다.

```java
// 조회 메서드는 항상 deletedAt IS NULL 필터 포함
rawMaterialRepository.findAllByDeletedAtIsNull()
rawMaterialRepository.findByIdAndDeletedAtIsNull(id)
```

**② LOT 번호 자동 생성**

클라이언트가 `lotNo`를 주지 않으면 서버가 `LOT-{YYYYMMDD}-{SEQ3}` 형식으로 생성합니다.

```java
// MaterialService.generateLotNo()
String prefix = "LOT-" + LocalDate.now().format("yyyyMMdd") + "-";
long seq = lotRepository.countByLotNoStartingWith(prefix) + 1;
return String.format("%s%03d", prefix, seq);
// 결과: LOT-20260515-001
```

> **알려진 한계**: 동시 요청 시 race condition 가능성 있음.
> `lot_no` 컬럼의 UNIQUE 제약으로 충돌 시 예외 처리하고 있으며,
> 운영 전환 시 DB 시퀀스나 분산 ID 생성기 도입 예정.

**③ InspectionSpec — 버전 관리 (supersede 방식)**

검사기준이 바뀌면 기존 기준을 삭제하지 않고 `superseded_at`을 채우고,
신규 기준을 version +1로 새로 생성합니다.

```
PATCH /api/materials/specs/{id} 호출 시:
  1. 기존 spec.supersede() → superseded_at = now()
  2. 신규 spec 저장       → version = 기존 + 1
```

이렇게 하면 과거 검사 기록이 당시 적용된 기준 버전을 그대로 가리킵니다.
GMP 감사에서 "당시 기준이 무엇이었느냐"를 소명하는 데 필요한 구조입니다.

**④ 검사기준 NUMERIC / VISUAL 분기**

```java
public enum MeasureType {
    NUMERIC, // minValue / maxValue 범위로 서버가 자동 판정
    VISUAL   // 검사자가 직접 PASS / FAIL 선택
}
```

예를 들어 "배터리 전압 3.7V~4.2V"는 NUMERIC, `minValue=3.7`, `maxValue=4.2`로 등록하면
실측값 입력 시 서버가 자동으로 범위를 판정합니다.
"외관 이상 유/무"는 VISUAL로 등록해 검사자가 직접 선택합니다.

**InspectionCategory Enum이 기본값을 추천**합니다.

```java
DIMENSION   → MEASURE + CALIPER + NUMERIC + "mm"  // 치수 검사 기본 세트
APPEARANCE  → VISUAL_CHECK + VISUAL_INSPECTION + VISUAL  // 외관 검사 기본 세트
ELECTRICAL  → MEASURE + MULTIMETER + NUMERIC + "V" // 전기 검사 기본 세트
```

사용자가 카테고리를 선택하면 프론트에서 나머지 필드를 자동 추천해 입력 부담을 줄입니다.

---

## 도메인 2 — `inspection` : 수입검사 자동 판정

### 이 도메인이 핵심인 이유

종이 검사표를 DB로 전환하는 것 자체가 목적이 아닙니다.
**"검사자가 항목별 측정값을 입력하면 시스템이 자동으로 합격/불합격을 판정하고,
그 결과가 LOT 상태에 즉시 반영되는 것"** 이 핵심입니다.

### 검사 등록 흐름 — 단일 트랜잭션

```java
@Transactional
public InspectionResponse createInspection(InspectionRequest request) {
    // 1. LOT 조회
    Lot lot = materialService.findLotEntityById(request.lotId());

    // 2. InspectionRecord 생성 (검사 헤더)
    InspectionRecord record = InspectionRecord.builder()
        .lot(lot).inspector(inspector).note(request.note()).build();
    recordRepository.save(record);

    // 3. 항목별 InspectionDetail 생성
    List<InspectionDetail> details = request.details().stream()
        .map(item -> buildDetail(record, item))  // spec-material 검증 포함
        .toList();
    detailRepository.saveAll(details);

    // 4. 자동 합격 판정: 하나라도 FAIL이면 전체 FAIL
    boolean anyFail = details.stream().anyMatch(d -> d.getResult() == FAIL);
    InspectionResult overall = anyFail ? FAIL : PASS;

    // 5. Record에 종합 결과 기록
    record.conclude(overall);

    // 6. LOT 상태 전이 (PENDING → PASS or FAIL)
    lot.applyInspectionResult(anyFail ? LotStatus.FAIL : LotStatus.PASS);
}
```

1~6 전체가 하나의 트랜잭션입니다.
3번 항목 저장 중 예외가 발생하면 LOT 상태 전이까지 모두 롤백됩니다.

### NUMERIC 자동 판정 로직

```java
private InspectionResult judgeNumeric(InspectionSpec spec, String measuredValue) {
    double value = Double.parseDouble(measuredValue);
    boolean inRange =
        (spec.getMinValue() == null || value >= spec.getMinValue()) &&
        (spec.getMaxValue() == null || value <= spec.getMaxValue());
    return inRange ? PASS : FAIL;
}
```

spec의 `measureType == NUMERIC`이면 이 메서드로 자동 판정,
`VISUAL`이면 클라이언트가 보낸 result 값을 그대로 사용합니다.

### spec-material 일치 검증

```java
Long lotMaterialId = record.getLot().getRawMaterial().getId();
if (!spec.getRawMaterial().getId().equals(lotMaterialId)) {
    throw new BusinessException(InspectionErrorCode.SPEC_MATERIAL_MISMATCH);
}
```

예: 실리콘 시트 LOT에 배터리 검사기준을 제출하면 차단합니다.
이 검증이 없으면 잘못된 기준으로 검사한 데이터가 저장될 수 있습니다.

---

## 도메인 3 — `material` 의 LOT 상태 머신

inspection 도메인과 연결되는 핵심 개념이라 별도로 설명합니다.

```
PENDING ──(수입검사 전체 PASS)──→ PASS    [InspectionService 자동]
PENDING ──(하나라도 FAIL)────────→ FAIL   [InspectionService 자동]
PENDING ──(관리자 수동)──────────→ HOLD   [PATCH /api/lots/{id}/status]
FAIL/HOLD ─(재검사 후)──────────→ PASS/FAIL
PASS ────────────────────────────→ HOLD   (불가, 예외 발생)
```

**상태 전이 규칙을 Entity 메서드 안에 배치한 이유**:

```java
// Lot.java
public void holdManually() {
    if (this.status == LotStatus.PASS) {
        throw new BusinessException(INVALID_STATUS_TRANSITION); // PASS → HOLD 차단
    }
    this.status = LotStatus.HOLD;
}

public void applyInspectionResult(LotStatus result) {
    this.status = result; // 검사 완료 시 자동 전이 (제약 없음)
}
```

두 메서드를 분리해 수동 API와 자동 판정의 허용 범위를 명확히 구분했습니다.
Service에 if-else로 흩뿌리면 새 Service가 추가될 때 규칙을 빠뜨릴 수 있습니다.
Entity에 있으면 어떤 경로로 호출해도 규칙이 항상 적용됩니다.

---

## 도메인 4 — `production` : 설비 · 생산 이력 · 알람

### 생산 투입 통제 — 시스템의 핵심 안전망

```java
// ProductionService.createProductionLog()
if (lot.getStatus() != LotStatus.PASS) {
    throw new BusinessException(ProductionErrorCode.LOT_NOT_PASSED);
}
```

이 한 줄이 불량 원자재가 생산 라인에 투입되는 것을 소프트웨어 레벨에서 차단합니다.
사업계획서의 "불량 LOT가 생산에 투입되는 것을 시스템적으로 차단"에 직접 대응합니다.

### 알람 해소 멱등성 보호

```java
if (alarm.isResolved()) {
    throw new BusinessException(ALARM_ALREADY_RESOLVED); // 400
}
alarm.resolve();
```

이미 해소된 알람을 다시 해소하려 하면 400을 반환합니다.
의도치 않은 중복 API 호출에 대한 방어입니다.

### 대시보드 API

`GET /api/dashboard` 하나로 네 가지 지표를 집계해 반환합니다.

```java
// DashboardService.getDashboard()
int todayProducedQty      = productionLogRepository.sumTodayProducedQty(startOfDay);
long activeAlarmCount     = alarmLogRepository.countByResolvedAtIsNull();
double equipmentRunRate   = (double) runningCount / totalCount; // RUNNING 비율
long pendingLotCount      = lotRepository.countByStatusAndDeletedAtIsNull(PENDING);
```

---

## 핵심 API — `GET /api/lots/{id}/history`

이 시스템에서 가장 중요한 API입니다.
LOT 번호 하나로 입고 정보 + 검사 이력 전체 + 생산 이력 전체를 단일 응답으로 반환합니다.

```json
{
  "lotInfo": {
    "lotNo": "LOT-20260515-001",
    "materialCode": "ECG-MAIN-001",
    "materialName": "심전계 메인보드",
    "quantity": 100,
    "status": "PASS"
  },
  "inspections": [
    {
      "overallResult": "PASS",
      "inspectedAt": "2026-05-15T10:00:00",
      "inspectorName": "김검사",
      "details": [
        { "itemName": "이상 유/무", "specDesc": "육안 이상 없을 것", "measuredValue": "이상없음", "result": "PASS" },
        { "itemName": "ECG 성능",  "specDesc": "ECG 신호 정상 반응", "measuredValue": "정상",    "result": "PASS" }
      ]
    }
  ],
  "productionLogs": [
    { "processName": "조립", "producedQty": 98, "defectQty": 2, "startedAt": "..." }
  ]
}
```

**N+1 방지 처리**: `LotHistoryService`는 record ID 목록으로 detail을 일괄 조회합니다.

```java
// N+1 방지 — record별로 detail 개별 조회하지 않음
List<Long> recordIds = records.stream().map(InspectionRecord::getId).toList();
Map<Long, List<InspectionDetail>> detailMap = detailRepository
    .findByRecord_IdIn(recordIds).stream()  // 단일 IN 쿼리
    .collect(Collectors.groupingBy(d -> d.getRecord().getId()));
```

---

## 보안 설계

### JWT Stateless 인증

```
HTTP Request
  → JwtAuthenticationFilter (Bearer 토큰 파싱, SecurityContext 설정)
  → Controller (@PreAuthorize로 역할 검증)
```

토큰이 없거나 잘못된 경우 필터에서 직접 응답하지 않고 인증 없는 상태로 체인을 통과시킵니다.
이후 `anyRequest().authenticated()` 규칙에서 `MesAuthenticationEntryPoint`가 401을 반환합니다.
필터와 보안 정책을 분리하는 Spring Security 권장 패턴입니다.

### 역할 기반 접근 제어

| 역할 | 허용 작업 |
|---|---|
| `ADMIN` | 마스터 데이터 변경 (원자재/설비 등록, 삭제) |
| `INSPECTOR` | 수입검사 등록 |
| `OPERATOR` | 생산 이력 등록, 알람 처리, 설비 상태 변경 |
| 전체 인증 사용자 | 조회 API 전체 |

```java
// 예시
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/api/materials")

@PreAuthorize("hasAnyRole('ADMIN', 'INSPECTOR')")
@PostMapping("/api/inspections")
```

### 비밀번호 BCrypt, JWT Secret 환경변수 주입

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET:dev-secret-key-change-in-production}
  expiration-ms: ${JWT_EXPIRATION_MS:3600000}
```

커밋에 비밀키가 포함되지 않도록 환경변수로 주입합니다.

---

## 공통 응답 포맷 — ApiResponse

모든 API 응답은 `ApiResponse<T>`로 감싸서 반환합니다.

```json
// 성공
{ "success": true, "message": "성공", "data": { ... } }

// 실패
{ "success": false, "message": "원자재를 찾을 수 없습니다.", "data": null }
```

**에러 코드 설계**: 도메인별 enum이 `ErrorCode` 인터페이스를 구현합니다.

```java
public interface ErrorCode {
    int getStatus();
    String getMessage();
}

// 각 도메인이 독립적으로 에러 코드를 가짐
MaterialErrorCode.MATERIAL_NOT_FOUND  → 404
InspectionErrorCode.SPEC_MATERIAL_MISMATCH → 400
ProductionErrorCode.LOT_NOT_PASSED → 400
```

`GlobalExceptionHandler`는 도메인을 몰라도 `e.getErrorCode().getStatus()`로 응답 상태를 결정합니다.
새 도메인 추가 시 `GlobalExceptionHandler` 수정 없이 도메인 enum만 추가하면 됩니다.

---

## 현재 알려진 한계 (의도적 결정)

| 한계 | 이유 | 운영 전환 시 대응 |
|---|---|---|
| `ddl-auto: create-drop` | 개발 중 스키마 변경 잦음 | `validate` + Flyway 마이그레이션 |
| Refresh Token 미구현 | OJT 범위 | Refresh Token + 토큰 회전 도입 |
| 페이지네이션 없음 | OJT 범위 | `Pageable` + `Page<T>` 반환 |
| LOT 번호 race condition | 운영 환경 아님 | DB SEQUENCE 또는 UUID |
| PLC/SCADA 연동 없음 | 하드웨어 의존, 범위 외 | 향후 시리얼/OPC-UA 연동 |
| AI 비전 검사 없음 | 사업계획서 2년차 범위 | VisionInspection 엔티티만 스켈레톤 유지 |

---

## 실행 방법

```powershell
# PostgreSQL 실행 후
cd backend
.\gradlew.bat bootRun

# 앱 최초 실행 시 DataInitializer가 자동으로 시드 데이터를 등록한다.
# ▶ 계정 3개
#   admin     / admin123      (관리자 — 마스터 데이터 등록·삭제)
#   inspector / inspector123  (검사담당자 — 수입검사 등록)
#   operator  / operator123   (생산작업자 — 생산이력 등록·알람 처리)
# ▶ 원자재 6종 (RM-001~006): 펟패치A/B, 멤브레인, Main Board, 배터리, LCD
# ▶ 검사기준 20건: NUMERIC/VISUAL 대입, 캘리퍼/멀티미터/ECG 시뮬레이터 등
# ▶ 설비 4종: ECG 시뮬레이터, 캂리퍼, 멀티미터, 스톱워치
# Swagger UI: http://localhost:8080/swagger-ui.html

# 프론트엔드
cd frontend
npm run dev
# http://localhost:5173
```

---

## 시연 순서 (권장)

1. **실행 + 자동 시드 확인** → `bootRun` 시 로그에 "원자재 6종 + 검사기준 시드 데이터 생성 완료" 출력 확인
2. **로그인** → `POST /api/auth/login` (admin / admin123) → JWT 발급 확인
3. **원자재 목록 조회** → `GET /api/materials` → 시드된 6종(펫패치A/B, 멤브레인, Main Board, 배터리, LCD) 확인
4. **검사기준 조회** → `GET /api/materials/{배터리 id}/specs` → NUMERIC 전압 기준 minValue=3.7 / maxValue=4.2 확인
5. **LOT 입고** → `POST /api/lots` (rawMaterialId = 배터리 ID, 수량 50) → 상태 `PENDING` 확인
6. **수입검사 등록 (FAIL 시나리오)** → `POST /api/inspections` → 전압 3.5V 입력 → NUMERIC 자동 FAIL 판정 → LOT 상태 `FAIL` 전이 확인
7. **생산 투입 시도** → `POST /api/production-logs` (FAIL LOT) → `LOT_NOT_PASSED` 400 에러 확인
8. **재검사 (PASS 시나리오)** → `POST /api/inspections` → 전압 3.9V 입력 → NUMERIC 자동 PASS 판정 → LOT 상태 `PASS` 전이 확인
9. **생산 이력 등록** → `POST /api/production-logs` (PASS LOT) → 정상 저장 확인
10. **LOT 전체 이력 조회** → `GET /api/lots/{id}/history` → 입고 + 검사 2건 + 생산 통합 응답 확인
11. **대시보드** → `GET /api/dashboard` → 당일 생산량 / 알람 수 / 설비 가동률 / PENDING LOT 수 확인
