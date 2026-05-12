# CLAUDE.md — MES 의료기기 원자재 이력관리 시스템

> 이 파일은 Claude Code가 이 프로젝트를 작업할 때 항상 먼저 읽어야 하는 컨텍스트 문서입니다.
> 코드 생성, 리팩토링, 테스트 작성 등 모든 작업 전에 이 문서 전체를 참고하세요.

---

## 1. 프로젝트 개요

### 1.1 목적

중소기업 기술개발(R&D) 지원사업의 일환으로 개발되는 **Cloud Web 기반 의료장비 원자재 AI 비전 검사 및 이력 추적 모니터링 시스템**의 백엔드 서버입니다.

현재 작업자가 종이 서류에 수기로 작성하는 수입검사 이력을 디지털화하고, LOT NO 기반으로 원자재의 입고→검사→생산 전 과정을 추적할 수 있도록 합니다.

### 1.2 배경 및 문제

- 바이오센서(펫패치A/B, 멤브레인)와 심전계(Main Board, 배터리, LCD) 원자재의 수입검사를 작업자가 수기로 기록 중
- 수기 작업으로 인한 오기, 누락, 데이터 분실 발생
- 원자재별 이력 추적이 불가능하여 품질 문제 발생 시 원인 파악 곤란
- AI 비전 카메라를 통한 치수 자동 측정 및 불량 판정 자동화 필요

### 1.3 핵심 가치

1. **LOT NO 기반 추적** — 원자재 입고부터 생산 투입까지 하나의 LOT 번호로 전 이력 조회 가능
2. **수입검사 디지털화** — 종이 검사기준서를 DB화하여 검사자가 화면에서 바로 입력
3. **AI 비전 검사 연동** — 비전 카메라 측정값을 서버에 자동 저장
4. **생산 모니터링** — 설비 가동률, 알람, 금일 생산량 실시간 확인

### 1.4 개발 범위 (OJT 기준)

이 프로젝트는 신입 OJT 교육 목적의 구현입니다. 아래 범위 내에서 작업합니다.

| 단계 | 내용 | 우선순위 |
|------|------|----------|
| 1단계 | 원자재 마스터, LOT 입고 등록, 유저 CRUD | 최우선 |
| 2단계 | 검사기준 등록, 수입검사 기록 (Record + Detail) | 핵심 |
| 3단계 | AI 비전 검사 결과 저장 | 중요 |
| 4단계 | 생산이력, 설비, 알람 모니터링 | 확장 |

---

## 2. 기술 스택

### 2.1 백엔드

| 항목 | 기술 | 버전 | 비고 |
|------|------|------|------|
| Language | Java | 17 | LTS |
| Framework | Spring Boot | 3.x | |
| ORM | Spring Data JPA | — | Hibernate |
| DB | MySQL | 8.x | |
| Build | Gradle | — | |
| API Docs | Springdoc OpenAPI (Swagger) | 2.x | |
| Validation | Spring Validation | — | Bean Validation |
| Test | JUnit 5 + Mockito | — | |

### 2.2 인프라 (예정)

| 항목 | 기술 | 비고 |
|------|------|------|
| Cloud | AWS or 온프레미스 | 미확정 |
| DB Host | RDS MySQL or Local | 개발 중 Local |
| Storage | 로컬 파일 서버 | 비전 이미지 저장 |

### 2.3 개발 환경

```
IDE          : IntelliJ IDEA
Java         : 17 (Amazon Corretto 권장)
MySQL        : 8.x (Docker 또는 로컬 설치)
API 테스트   : Postman 또는 Swagger UI
버전 관리    : Git
```

---

## 3. 도메인 모델

### 3.1 핵심 엔티티 관계

```
RAW_MATERIAL (원자재 마스터)
  │
  ├─── INSPECTION_SPEC (검사기준 마스터)  ←── 1회 세팅, 거의 변경 없음
  │
  └─── LOT (입고 단위)  ←── LOT_NO가 모든 이력의 추적 키
         │
         ├─── INSPECTION_RECORD (수입검사 1건)
         │         └─── INSPECTION_DETAIL (항목별 세부 결과)
         │                   └─── (ref) INSPECTION_SPEC
         │
         ├─── VISION_INSPECTION (AI 비전 검사 결과)
         │
         └─── PRODUCTION_LOG (생산 공정 이력)
                   └─── (ref) EQUIPMENT (설비)
                                └─── ALARM_LOG (알람 이력)

USER  ←── INSPECTION_RECORD 검사 수행자
```

### 3.2 엔티티 상세 정의

#### RAW_MATERIAL — 원자재 마스터

```
필드명          타입            제약            설명
id              INT             PK, AUTO        원자재 ID
code            VARCHAR(50)     UNIQUE, NN      원자재 코드 (예: RM-001)
name            VARCHAR(100)    NN              원자재명 (예: 펫패치A, Main Board)
category        VARCHAR(50)                     분류 (biosensor | ecg)
unit            VARCHAR(20)                     단위 (ea, g, m 등)
spec_standard   VARCHAR(200)                    자체규격 기준 요약
created_at      DATETIME        DEFAULT NOW()
```

**실제 데이터 예시:**

| code | name | category | unit |
|------|------|----------|------|
| RM-001 | 펫패치A | biosensor | ea |
| RM-002 | 펫패치B | biosensor | ea |
| RM-003 | 멤브레인 | biosensor | ea |
| RM-004 | Main Board | ecg | ea |
| RM-005 | 배터리 | ecg | ea |
| RM-006 | LCD | ecg | ea |

---

#### INSPECTION_SPEC — 검사기준 마스터

수입검사기준서(IS-2503-Q-01 등) 내용을 DB화한 테이블입니다.

```
필드명          타입            제약            설명
id              INT             PK, AUTO
raw_material_id INT             FK → RAW_MATERIAL.id, NN
item_name       VARCHAR(100)    NN              검사항목명
spec_desc       VARCHAR(300)                    규격(Spec) 내용
method          VARCHAR(50)                     검사방법
equipment       VARCHAR(100)                    측정기기
timing          VARCHAR(50)                     주기 (입고시)
```

**실제 데이터 예시 (펫패치A 기준):**

| raw_material_id | item_name | spec_desc | method | equipment |
|-----------------|-----------|-----------|--------|-----------|
| 1 | 치수 | 30mm ± 1mm | 측정 | 내/외측 캘리퍼 |
| 1 | 미성형 | 미성형 부분이 없을 것 | 육안 | 육안확인 |
| 1 | 오염 | 흑점 등의 오염이 없을 것 | 육안 | 육안확인 |
| 1 | 이상 유무 | 힘 등의 이상이 없을 것 | 육안 | 육안확인 |

**Main Board 기준:**

| raw_material_id | item_name | spec_desc | method | equipment |
|-----------------|-----------|-----------|--------|-----------|
| 4 | 이상 유무 | 패짐, 힘 등의 이상이 없을 것 | 육안 | 육안확인 |
| 4 | 납땜 | 납땜이 정상적으로 되었을 것 | 육안 | 육안확인 |
| 4 | ECG 성능 | ECG 신호에 정상적으로 반응할 것 | TEST | ECG시뮬레이터 |
| 4 | 정상작동 | 부팅 및 블루투스 등 기능이 정상적으로 작동할 것 | TEST | 본체 |

---

#### LOT — 입고 단위

```
필드명          타입            제약            설명
id              INT             PK, AUTO
lot_no          VARCHAR(50)     UNIQUE, NN      LOT 번호 (모든 이력의 추적 키)
raw_material_id INT             FK → RAW_MATERIAL.id, NN
quantity        INT             NN              입고 수량
received_at     DATETIME                        입고일시
supplier        VARCHAR(100)                    공급업체명
status          VARCHAR(20)     DEFAULT pending  pending | pass | fail | hold
```

**LOT 번호 생성 규칙 (예시):**
```
LOT-{YYYYMMDD}-{SEQ3}
예: LOT-20260512-001
```

---

#### INSPECTION_RECORD — 수입검사 건

```
필드명          타입            제약            설명
id              INT             PK, AUTO
lot_id          INT             FK → LOT.id, NN
inspector_id    INT             FK → USER.id
overall_result  VARCHAR(10)                     pass | fail (Detail 전체 기반)
note            VARCHAR(500)                    특이사항
inspected_at    DATETIME        DEFAULT NOW()
```

---

#### INSPECTION_DETAIL — 검사항목별 세부 결과

```
필드명                  타입            제약            설명
id                      INT             PK, AUTO
inspection_record_id    INT             FK → INSPECTION_RECORD.id, NN
inspection_spec_id      INT             FK → INSPECTION_SPEC.id, NN
measured_value          VARCHAR(100)                    실측값 (예: 30.2mm, pass, 3.8V)
result                  VARCHAR(10)                     pass | fail
```

---

#### VISION_INSPECTION — AI 비전 검사 결과

```
필드명              타입            제약            설명
id                  INT             PK, AUTO
lot_id              INT             FK → LOT.id, NN
measured_length     FLOAT                           측정 치수(mm), 목표 오차 1mm 이하
measured_hole_size  FLOAT                           타공홀 크기(mm)
defect_result       VARCHAR(10)                     pass | fail
accuracy_score      FLOAT                           AI 인식 정확도(%), 목표 99% 이상
image_path          VARCHAR(500)                    촬영 이미지 저장 경로
inspected_at        DATETIME        DEFAULT NOW()
```

---

#### PRODUCTION_LOG — 생산 공정 이력

```
필드명          타입            제약            설명
id              INT             PK, AUTO
lot_id          INT             FK → LOT.id, NN
process_name    VARCHAR(100)                    공정명 (세척 | 조립 | 검사 등)
equipment_id    INT             FK → EQUIPMENT.id
produced_qty    INT                             생산 수량
defect_qty      INT             DEFAULT 0       불량 수량
started_at      DATETIME
ended_at        DATETIME
```

---

#### EQUIPMENT — 설비 마스터

```
필드명              타입            제약            설명
id                  INT             PK, AUTO
equipment_code      VARCHAR(50)     UNIQUE, NN      설비 코드
name                VARCHAR(100)    NN              설비명
status              VARCHAR(20)                     running | idle | error | maintenance
last_maintained_at  DATETIME                        최근 점검일시
```

---

#### ALARM_LOG — 설비 알람 이력

```
필드명          타입            제약            설명
id              INT             PK, AUTO
equipment_id    INT             FK → EQUIPMENT.id, NN
alarm_code      VARCHAR(50)                     알람 코드
message         VARCHAR(500)                    알람 메시지
severity        VARCHAR(20)                     info | warning | critical
occurred_at     DATETIME        DEFAULT NOW()
resolved_at     DATETIME                        해소일시 (null이면 미해소)
```

---

#### USER — 사용자

```
필드명          타입            제약            설명
id              INT             PK, AUTO
username        VARCHAR(50)     UNIQUE, NN      로그인 ID
role            VARCHAR(20)                     admin | inspector | operator
created_at      DATETIME        DEFAULT NOW()
```

---

## 4. 패키지 구조

Package by Feature + Layered Architecture 기반입니다.
도메인 연관성을 기준으로 3개 기능 패키지로 묶었습니다.

```
src/main/java/com/company/mes/
│
├── MesApplication.java
│
├── common/
│   ├── ApiResponse.java             # 공통 응답 포맷 { success, data, message }
│   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   └── enums/
│       ├── LotStatus.java           # PENDING, PASS, FAIL, HOLD
│       ├── InspectionResult.java    # PASS, FAIL
│       └── UserRole.java            # ADMIN, INSPECTOR, OPERATOR
│
├── material/                        # 원자재 + LOT + 검사기준
│   ├── entity/
│   │   ├── RawMaterial.java
│   │   ├── Lot.java
│   │   └── InspectionSpec.java
│   ├── repository/
│   │   ├── RawMaterialRepository.java
│   │   ├── LotRepository.java
│   │   └── InspectionSpecRepository.java
│   ├── service/
│   │   └── MaterialService.java
│   ├── controller/
│   │   └── MaterialController.java
│   └── dto/
│       ├── RawMaterialRequest.java
│       ├── RawMaterialResponse.java
│       ├── LotRequest.java
│       └── LotResponse.java
│
├── inspection/                      # 수입검사 + AI 비전검사
│   ├── entity/
│   │   ├── InspectionRecord.java
│   │   ├── InspectionDetail.java
│   │   └── VisionInspection.java
│   ├── repository/
│   │   ├── InspectionRecordRepository.java
│   │   ├── InspectionDetailRepository.java
│   │   └── VisionInspectionRepository.java
│   ├── service/
│   │   └── InspectionService.java
│   ├── controller/
│   │   └── InspectionController.java
│   └── dto/
│       ├── InspectionRequest.java
│       ├── InspectionDetailItem.java
│       └── InspectionResponse.java
│
└── production/                      # 생산이력 + 설비 + 알람 + 유저
    ├── entity/
    │   ├── ProductionLog.java
    │   ├── Equipment.java
    │   ├── AlarmLog.java
    │   └── User.java
    ├── repository/
    │   ├── ProductionLogRepository.java
    │   ├── EquipmentRepository.java
    │   ├── AlarmLogRepository.java
    │   └── UserRepository.java
    ├── service/
    │   └── ProductionService.java
    ├── controller/
    │   └── ProductionController.java
    └── dto/
        ├── ProductionRequest.java
        └── AlarmRequest.java
```

---

## 5. API 설계

### 5.1 공통 응답 포맷

모든 API는 아래 포맷으로 응답합니다.

```json
{
  "success": true,
  "data": { ... },
  "message": "정상 처리되었습니다."
}
```

오류 응답:
```json
{
  "success": false,
  "data": null,
  "message": "LOT를 찾을 수 없습니다."
}
```

### 5.2 API 목록

#### Material API

```
GET    /api/materials              원자재 목록 조회
POST   /api/materials              원자재 등록
GET    /api/materials/{id}         원자재 단건 조회

GET    /api/lots                   LOT 목록 조회
POST   /api/lots                   LOT 등록 (입고 등록)
GET    /api/lots/{id}              LOT 단건 조회
GET    /api/lots/{id}/history      LOT 전체 이력 조회 (핵심 API)
PATCH  /api/lots/{id}/status       LOT 상태 변경

GET    /api/materials/{id}/specs   원자재별 검사기준 조회
POST   /api/specs                  검사기준 등록
```

#### Inspection API

```
POST   /api/inspections            수입검사 등록 (Record + Detail 한번에)
GET    /api/inspections/{id}       검사 결과 단건 조회
GET    /api/lots/{lotId}/inspections  LOT별 검사 이력 조회

POST   /api/vision-inspections     AI 비전 검사 결과 저장
GET    /api/lots/{lotId}/vision    LOT별 비전 검사 결과 조회
```

#### Production API

```
POST   /api/production-logs        생산 이력 등록
GET    /api/production-logs        생산 이력 목록 (금일 생산량 포함)

GET    /api/equipment              설비 목록 조회
PATCH  /api/equipment/{id}/status  설비 상태 변경

GET    /api/alarms                 알람 목록 조회
POST   /api/alarms                 알람 등록
PATCH  /api/alarms/{id}/resolve    알람 해소 처리

GET    /api/users                  사용자 목록
POST   /api/users                  사용자 등록
```

### 5.3 핵심 API 상세 — LOT 이력 조회

`GET /api/lots/{id}/history` 는 이 시스템의 존재 이유를 보여주는 API입니다.
하나의 LOT NO로 입고부터 생산까지 전체 이력을 반환합니다.

```json
{
  "success": true,
  "data": {
    "lot": {
      "lotNo": "LOT-20260512-001",
      "rawMaterialName": "펫패치A",
      "quantity": 500,
      "receivedAt": "2026-05-12T09:00:00",
      "status": "pass"
    },
    "inspectionRecord": {
      "overallResult": "pass",
      "inspectedAt": "2026-05-12T10:30:00",
      "inspectorName": "홍길동",
      "details": [
        { "itemName": "치수",    "specDesc": "30mm ± 1mm", "measuredValue": "30.2mm", "result": "pass" },
        { "itemName": "미성형", "specDesc": "없을 것",     "measuredValue": "없음",   "result": "pass" },
        { "itemName": "오염",   "specDesc": "없을 것",     "measuredValue": "없음",   "result": "pass" }
      ]
    },
    "visionInspection": {
      "measuredLength": 30.1,
      "defectResult": "pass",
      "accuracyScore": 99.3,
      "inspectedAt": "2026-05-12T10:45:00"
    },
    "productionLogs": [
      {
        "processName": "세척",
        "equipmentName": "세척기 #1",
        "producedQty": 500,
        "defectQty": 2,
        "startedAt": "2026-05-12T13:00:00"
      }
    ]
  }
}
```

### 5.4 핵심 API 상세 — 수입검사 등록

`POST /api/inspections`

검사 등록 시 InspectionRecord 1건 + InspectionDetail N건이 단일 트랜잭션으로 저장됩니다.
Detail 중 하나라도 fail이면 overall_result는 자동으로 fail, LOT status도 fail로 업데이트됩니다.

```json
// Request
{
  "lotId": 1,
  "inspectorId": 2,
  "note": "이상 없음",
  "details": [
    { "inspectionSpecId": 1, "measuredValue": "30.2mm", "result": "pass" },
    { "inspectionSpecId": 2, "measuredValue": "없음",   "result": "pass" },
    { "inspectionSpecId": 3, "measuredValue": "없음",   "result": "pass" },
    { "inspectionSpecId": 4, "measuredValue": "없음",   "result": "pass" }
  ]
}
```

---

## 6. 비즈니스 로직 규칙

Claude Code가 코드를 생성하거나 수정할 때 반드시 지켜야 할 규칙입니다.

### 6.1 LOT 상태 전이 규칙

```
pending → pass   : 수입검사 전체 항목 pass 시 자동 전환
pending → fail   : 수입검사 중 하나라도 fail 시 자동 전환
pending → hold   : 관리자가 수동으로 보류 처리
fail    → hold   : 재검토 필요 시 관리자 수동 전환
hold    → pass   : 재검사 후 pass 시
hold    → fail   : 재검사 후 fail 시
```

**pass 상태의 LOT만 생산 투입(PRODUCTION_LOG 등록)이 가능합니다.**

### 6.2 overall_result 자동 계산

InspectionService에서 수입검사 등록 시 아래 로직을 수행합니다:

```
1. InspectionRecord 저장
2. InspectionDetail 전체 저장
3. Detail 중 result = 'fail' 이 하나라도 있으면 → overall_result = 'fail'
4. 전체 pass이면 → overall_result = 'pass'
5. LOT.status를 overall_result 값으로 업데이트
```

이 로직은 반드시 `@Transactional` 단일 트랜잭션 안에서 수행해야 합니다.

### 6.3 검사기준(INSPECTION_SPEC) 조회 규칙

수입검사 화면에서 검사항목을 불러올 때:
- LOT의 raw_material_id로 INSPECTION_SPEC을 조회하여 검사항목 목록을 반환합니다.
- INSPECTION_SPEC이 등록되지 않은 원자재는 검사 등록 불가 처리합니다.

### 6.4 AI 비전 검사 정확도 기준

- 원자재 인식 정확도: **99% 이상** (accuracy_score 기준)
- 치수(길이) 오차: **1mm 이하** (measured_length 기준)
- 타공홀 크기 오차: **1mm 이하** (measured_hole_size 기준)
- 기준 미달 시 defect_result = 'fail' 자동 처리

---

## 7. 코딩 컨벤션

### 7.1 Java 일반

```java
// 클래스명: PascalCase
public class InspectionService { }

// 메서드/변수명: camelCase
public InspectionResponse createInspection(InspectionRequest request) { }

// 상수: UPPER_SNAKE_CASE
public static final String DEFAULT_STATUS = "pending";

// 들여쓰기: 4 spaces (tab 금지)
```

### 7.2 Entity 작성 규칙

```java
@Entity
@Table(name = "inspection_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 기본 생성자
public class InspectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연관관계는 지연 로딩 기본
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    // Enum은 String으로 저장
    @Enumerated(EnumType.STRING)
    @Column(name = "overall_result", length = 10)
    private InspectionResult overallResult;

    @Column(name = "inspected_at")
    private LocalDateTime inspectedAt;

    // 생성자는 정적 팩토리 메서드 또는 Builder 사용
    @Builder
    public InspectionRecord(Lot lot, User inspector, String note) {
        this.lot = lot;
        this.inspector = inspector;
        this.note = note;
        this.inspectedAt = LocalDateTime.now();
    }
}
```

### 7.3 Service 작성 규칙

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)   // 기본은 readOnly
public class InspectionService {

    private final InspectionRecordRepository recordRepository;
    private final InspectionDetailRepository detailRepository;
    private final LotRepository lotRepository;

    @Transactional    // 쓰기 작업에만 명시
    public InspectionResponse createInspection(InspectionRequest request) {
        // 비즈니스 로직
    }
}
```

### 7.4 Controller 작성 규칙

```java
@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @PostMapping
    public ResponseEntity<ApiResponse<InspectionResponse>> create(
            @RequestBody @Valid InspectionRequest request) {
        InspectionResponse response = inspectionService.createInspection(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### 7.5 DTO 규칙

- Request DTO는 `@Valid` + Bean Validation 어노테이션 사용
- Response DTO는 Entity를 직접 반환하지 않음 (순환참조 방지)
- Entity → DTO 변환은 Service 레이어에서 처리

```java
// Request 예시
public class LotRequest {
    @NotNull(message = "원자재 ID는 필수입니다.")
    private Long rawMaterialId;

    @NotBlank(message = "LOT 번호는 필수입니다.")
    private String lotNo;

    @Positive(message = "수량은 양수여야 합니다.")
    private Integer quantity;
}
```

### 7.6 예외 처리 규칙

- 비즈니스 예외는 `BusinessException` (커스텀 예외) 사용
- `GlobalExceptionHandler`에서 일괄 처리
- 404: 엔티티 미존재, 400: 유효성 검증 실패, 409: 중복 데이터

```java
// 엔티티 조회 시
Lot lot = lotRepository.findById(id)
    .orElseThrow(() -> new BusinessException("LOT를 찾을 수 없습니다. id: " + id));
```

---

## 8. DB 설정 및 DDL

### 8.1 application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mes_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate        # 운영: validate, 개발 초기: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect

  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher

springdoc:
  swagger-ui:
    path: /swagger-ui.html

server:
  port: 8080
```

### 8.2 DDL — 핵심 테이블

```sql
CREATE DATABASE mes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mes_db;

CREATE TABLE raw_material (
    id             INT          AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(50)  NOT NULL UNIQUE COMMENT '원자재 코드',
    name           VARCHAR(100) NOT NULL        COMMENT '원자재명',
    category       VARCHAR(50)                  COMMENT 'biosensor | ecg',
    unit           VARCHAR(20)                  COMMENT '단위',
    spec_standard  VARCHAR(200)                 COMMENT '자체규격 기준',
    created_at     DATETIME     DEFAULT NOW()
);

CREATE TABLE inspection_spec (
    id              INT          AUTO_INCREMENT PRIMARY KEY,
    raw_material_id INT          NOT NULL,
    item_name       VARCHAR(100) NOT NULL COMMENT '검사항목명',
    spec_desc       VARCHAR(300)          COMMENT '규격(Spec) 내용',
    method          VARCHAR(50)           COMMENT '검사방법',
    equipment       VARCHAR(100)          COMMENT '측정기기',
    timing          VARCHAR(50)           COMMENT '주기',
    FOREIGN KEY (raw_material_id) REFERENCES raw_material(id)
);

CREATE TABLE lot (
    id              INT         AUTO_INCREMENT PRIMARY KEY,
    lot_no          VARCHAR(50) NOT NULL UNIQUE COMMENT 'LOT 번호',
    raw_material_id INT         NOT NULL,
    quantity        INT         NOT NULL,
    received_at     DATETIME,
    supplier        VARCHAR(100),
    status          VARCHAR(20) DEFAULT 'pending' COMMENT 'pending|pass|fail|hold',
    FOREIGN KEY (raw_material_id) REFERENCES raw_material(id)
);

CREATE TABLE user (
    id         INT         AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50) NOT NULL UNIQUE,
    role       VARCHAR(20)          COMMENT 'admin|inspector|operator',
    created_at DATETIME    DEFAULT NOW()
);

CREATE TABLE inspection_record (
    id             INT          AUTO_INCREMENT PRIMARY KEY,
    lot_id         INT          NOT NULL,
    inspector_id   INT,
    overall_result VARCHAR(10)           COMMENT 'pass | fail',
    note           VARCHAR(500),
    inspected_at   DATETIME     DEFAULT NOW(),
    FOREIGN KEY (lot_id)       REFERENCES lot(id),
    FOREIGN KEY (inspector_id) REFERENCES user(id)
);

CREATE TABLE inspection_detail (
    id                   INT          AUTO_INCREMENT PRIMARY KEY,
    inspection_record_id INT          NOT NULL,
    inspection_spec_id   INT          NOT NULL,
    measured_value       VARCHAR(100) COMMENT '실측값',
    result               VARCHAR(10)  COMMENT 'pass | fail',
    FOREIGN KEY (inspection_record_id) REFERENCES inspection_record(id),
    FOREIGN KEY (inspection_spec_id)   REFERENCES inspection_spec(id)
);

CREATE TABLE vision_inspection (
    id                 INT          AUTO_INCREMENT PRIMARY KEY,
    lot_id             INT          NOT NULL,
    measured_length    FLOAT        COMMENT '측정 치수(mm)',
    measured_hole_size FLOAT        COMMENT '타공홀 크기(mm)',
    defect_result      VARCHAR(10)  COMMENT 'pass | fail',
    accuracy_score     FLOAT        COMMENT 'AI 정확도(%)',
    image_path         VARCHAR(500),
    inspected_at       DATETIME     DEFAULT NOW(),
    FOREIGN KEY (lot_id) REFERENCES lot(id)
);

CREATE TABLE equipment (
    id                 INT          AUTO_INCREMENT PRIMARY KEY,
    equipment_code     VARCHAR(50)  NOT NULL UNIQUE,
    name               VARCHAR(100) NOT NULL,
    status             VARCHAR(20)  COMMENT 'running|idle|error|maintenance',
    last_maintained_at DATETIME
);

CREATE TABLE production_log (
    id            INT          AUTO_INCREMENT PRIMARY KEY,
    lot_id        INT          NOT NULL,
    process_name  VARCHAR(100),
    equipment_id  INT,
    produced_qty  INT,
    defect_qty    INT          DEFAULT 0,
    started_at    DATETIME,
    ended_at      DATETIME,
    FOREIGN KEY (lot_id)       REFERENCES lot(id),
    FOREIGN KEY (equipment_id) REFERENCES equipment(id)
);

CREATE TABLE alarm_log (
    id            INT          AUTO_INCREMENT PRIMARY KEY,
    equipment_id  INT          NOT NULL,
    alarm_code    VARCHAR(50),
    message       VARCHAR(500),
    severity      VARCHAR(20)  COMMENT 'info|warning|critical',
    occurred_at   DATETIME     DEFAULT NOW(),
    resolved_at   DATETIME,
    FOREIGN KEY (equipment_id) REFERENCES equipment(id)
);
```

---

## 9. 테스트 전략

### 9.1 테스트 범위 (OJT 기준)

| 레이어 | 방법 | 우선순위 |
|--------|------|----------|
| Service | JUnit5 + Mockito 단위 테스트 | 최우선 |
| Repository | @DataJpaTest 슬라이스 테스트 | 중요 |
| Controller | @WebMvcTest + MockMvc | 여유 있으면 |

### 9.2 반드시 테스트해야 할 케이스

```
InspectionService
  - 수입검사 등록 시 전체 pass → LOT status가 pass로 변경되는지
  - 수입검사 등록 시 하나라도 fail → LOT status가 fail로 변경되는지
  - 트랜잭션 롤백: Detail 저장 중 예외 발생 시 Record도 롤백되는지

LotService
  - 존재하지 않는 LOT 조회 시 예외 발생하는지
  - 중복 LOT_NO 등록 시 예외 발생하는지
  - fail 상태 LOT에 생산 이력 등록 시 예외 발생하는지
```

---

## 10. Git 규칙

### 10.1 브랜치 전략

```
main          배포 브랜치 (직접 push 금지)
develop       통합 개발 브랜치
feature/xxx   기능 개발 브랜치 (develop에서 분기)
```

### 10.2 커밋 메시지 형식

```
feat: LOT 입고 등록 API 구현
fix: 수입검사 트랜잭션 롤백 오류 수정
refactor: InspectionService 로직 분리
test: LOT 상태 전이 단위 테스트 추가
docs: CLAUDE.md 검사기준 섹션 업데이트
chore: application.yml DB 설정 추가
```

---

## 11. docs 폴더 구성 안내

이 CLAUDE.md를 기반으로 아래 문서들을 추가로 작성할 예정입니다.

```
docs/
├── architecture/
│   ├── overview.md          시스템 전체 아키텍처 (레이어, 컴포넌트 구성)
│   ├── domain-model.md      도메인 모델 상세 (엔티티 관계, 상태 전이도)
│   └── api-design.md        API 설계 원칙 및 전체 목록
│
├── adr/                     Architecture Decision Record
│   ├── ADR-001-jpa-vs-mybatis.md       JPA 선택 이유
│   ├── ADR-002-package-by-feature.md   패키지 구조 결정 이유
│   └── ADR-003-lot-status-design.md    LOT 상태 관리 방식 결정
│
├── prd/
│   ├── requirements.md      기능 요구사항 전체 목록
│   ├── user-stories.md      사용자 스토리 (검사자/관리자/작업자 기준)
│   └── acceptance-criteria.md  기능별 완료 기준
│
└── dev/
    ├── setup.md             로컬 개발 환경 세팅 가이드
    ├── seed-data.md         초기 테스트 데이터 INSERT 쿼리
    └── trouble-shooting.md  자주 발생하는 오류 및 해결법
```

---

## 12. Claude Code 작업 지침

### 작업 시작 전 체크리스트

- [ ] 이 CLAUDE.md 전체를 읽었는가
- [ ] 작업 대상 패키지(material / inspection / production)를 확인했는가
- [ ] 관련 엔티티의 필드명과 타입이 섹션 3과 일치하는가

### 코드 생성 시 필수 준수 사항

1. Entity는 `@Builder` + `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 패턴 사용
2. 연관관계는 `FetchType.LAZY` 기본
3. 쓰기 작업 Service 메서드에 `@Transactional` 명시
4. Entity를 Controller에서 직접 반환하지 말 것 (항상 DTO 변환)
5. 예외는 `BusinessException` 사용, `GlobalExceptionHandler`에서 처리
6. 섹션 6의 비즈니스 로직 규칙을 반드시 준수

### 절대 하지 말아야 할 것

- `ddl-auto: create` 를 운영 환경에서 사용
- Entity 간 양방향 연관관계 무분별하게 추가 (N+1 주의)
- Service에서 다른 패키지의 Repository를 직접 주입 (패키지 경계 위반)
- Response DTO에 Entity 직접 포함