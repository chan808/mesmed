# medmes 프로젝트 브리핑 요약

> 상급자 보고용. 꼬리질문 대응을 위해 의사결정 근거·트레이드오프·예상 Q&A까지 포함.

---

## 1. 한 줄 요약

중소기업 의료장비 원자재 **수입검사 디지털화 + LOT 이력 추적** 백엔드.
수기 종이 기록을 DB로 옮기고, 원자재 입고 → 검사 → 생산 전 과정을 **LOT 번호 하나로 추적**한다.

- **포지셔닝**: R&D 사업계획서(3년 과제)의 1년차 "이력 데이터화 + 생산 모니터링" 영역을 백엔드 관점에서 OJT 학습용으로 슬라이스한 산출물.
- **현재 단계**: Phase 0~4 백엔드 핵심 도메인 완성. Phase 5(운영 준비) 미진입.
- **AI 비전·클라우드 이중화·PLC/시리얼 연동 등은 의도적으로 제외** (ADR에 근거 명시).

---

## 2. 기술 스택

| 영역 | 선택 | 비고 |
|---|---|---|
| Runtime | Java 17 | LTS |
| Framework | Spring Boot 4.0.6 | 회사 표준. springdoc 등 호환성 주의(ADR-001) |
| Build | Gradle | `./gradlew clean build / test / bootRun` |
| DB | PostgreSQL | 단일 인스턴스(localhost) |
| ORM | Spring Data JPA + Hibernate | `ddl-auto: create-drop` (개발), 운영 전환 시 `validate` |
| Auth | Spring Security Stateless + JWT (jjwt 0.12.x) | Access Token 단일, 만료 1시간 |
| 비밀번호 | BCrypt | `PasswordEncoderConfig` |
| 검증 | Bean Validation (Jakarta) | Record DTO에 직접 어노테이션 |
| 문서 | springdoc-openapi | `/swagger-ui.html` |
| 부가 | Lombok, Record DTO | |
| 컨테이너 | Docker | 설정 추가됨 (운영 배포 아직) |

**핵심 설계 선택**: Request/Response를 Java **Record**로 통일. 불변·간결·Validation 가독성.

---

## 3. 패키지/모듈 구조

```
com.chan.medmes
├── MedmesApplication.java
├── global/             공통 인프라
│   ├── response/       ApiResponse<T>
│   ├── error/          ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── security/       JWT (Provider/Filter/Properties/Config), MesPrincipal, EntryPoint
│   ├── OpenApiConfig
│   └── DataInitializer (원자재 6종·검사기준 20건·설비 4종·계정 3명 초기 시드)
├── auth/               로그인, JWT 발급
├── user/               사용자 CRUD, BCrypt
├── material/           원자재·LOT·검사기준 + LotHistoryService(이력 집계)
├── inspection/         수입검사 자동 판정 — 핵심 비즈니스 로직
└── production/         설비·생산 이력·알람·대시보드
```

### 패키지 의존 방향 (단방향 강제)

```
global ← user ← auth
global ← material
global ← inspection → material, user
global ← production → material
material(LotHistoryService) → inspection, production  (읽기 전용 예외)
```

- **규칙**: Service는 다른 패키지의 Repository를 직접 주입하지 않는다 → 다른 패키지의 Service를 통한다.
- **예외**: `LotHistoryService`만 다중 Repository 직접 주입 허용(읽기 전용 집계).
- 이유: 순환 의존 방지 + 변경 영향 범위 최소화 (ADR-002, ADR-005).

---

## 4. 요청·응답 흐름

```
HTTP Request
   ↓
Controller        @RestController, @Valid로 Record DTO 검증, @PreAuthorize 권한
   ↓
Service           @Transactional(readOnly=true) 기본 / 쓰기에만 @Transactional
                  Entity ↔ Record DTO 변환은 Service 또는 DTO.from(entity) 정적 팩토리
   ↓
Repository        JpaRepository<Entity, Long> + 파생 쿼리
   ↓
PostgreSQL
   ↑
ResponseEntity<ApiResponse<T>>   { success, message, data }
```

- **에러 처리**: `GlobalExceptionHandler`가
  - `BusinessException` → ErrorCode의 HTTP 상태 + 메시지
  - `MethodArgumentNotValidException` → 400 + 검증 메시지 join
  - 기타 → 500 + 내부 에러 로그
- **응답 포맷**: 항상 `ApiResponse<T>`로 감싸서 반환 (성공·실패 일관).

---

## 5. 도메인 모델 (ERD)

```
USER (id, username, password, displayName, role)
  ↓ inspector
INSPECTION_RECORD (id, lot_id, inspector_id, overall_result, note, inspected_at)
  ↓ FK
RAW_MATERIAL (id, code, name, category, unit, spec_standard, created_at, deleted_at[soft])
  ├── INSPECTION_SPEC (id, raw_material_id, item_name, spec_desc, method,
  │                    equipment, timing, version, superseded_at)   ← 버전 관리
  └── LOT (id, lot_no UNIQUE, raw_material_id, quantity, received_at,
           supplier, status, deleted_at[soft])
        ├── INSPECTION_RECORD ─→ INSPECTION_DETAIL (id, record_id, spec_id,
        │                                            measured_value, result, severity)
        └── PRODUCTION_LOG (id, lot_id, equipment_id, process_name,
                            produced_qty, defect_qty, started_at, ended_at)

EQUIPMENT (id, equipment_code UNIQUE, name, status, last_maintained_at)
  └── ALARM_LOG (id, equipment_id, alarm_code, message, severity,
                 occurred_at, resolved_at)

VISION_INSPECTION (id, lot_id, measured_length, measured_hole_size,
                   defect_result, accuracy_score, image_path, inspected_at)
  ← 3차 고도화 스켈레톤. 현재 사용 안 함.
```

### Enum 위치 규칙
- `LotStatus` → `material/` (현재 `enums/` 디렉토리 없이 패키지 직속 — 일관성 개선 여지)
- `InspectionResult`, `InspectionSeverity` → `inspection/enums/`
- `UserRole` → `user/` (마찬가지로 디렉토리 누락)
- `AlarmSeverity`, `EquipmentStatus` → `production/enums/`

---

## 6. 핵심 비즈니스 규칙

### 6.1 LOT 번호 자동 생성
- 클라이언트가 `lotNo`를 안 주면 서버에서 `LOT-{YYYYMMDD}-{SEQ3}` 형식으로 생성.
- 예: `LOT-20260514-001`
- 당일 prefix로 시작하는 LOT 수를 카운트해 +1 (`LotRepository.countByLotNoStartingWith`).
- **알려진 한계**: 동시 입고 시 race condition 가능 → 현재는 unique 제약(`lot_no`)으로 충돌 시 예외. 운영 전환 시 DB 시퀀스 또는 비관적 락 필요.

### 6.2 수입검사 자동 판정 (InspectionService.createInspection)

```
POST /api/inspections
   ↓ (단일 @Transactional)
1. Lot 조회 (없으면 LOT_NOT_FOUND)
2. InspectionRecord 저장
3. 요청 details[] 순회하며 InspectionDetail N개 생성·저장
   - 각 detail의 spec.rawMaterial == lot.rawMaterial 검증 (SPEC_MATERIAL_MISMATCH)
4. details 중 result == FAIL 하나라도 있으면 overall = FAIL, 아니면 PASS
5. record.conclude(overall) — InspectionRecord에 종합 결과 기록
6. lot.applyInspectionResult(...) — LOT 상태를 PASS 또는 FAIL로 전이
```

- **트랜잭션 보장**: Detail 저장 중 예외 발생 → Record까지 롤백.
- **PASS/FAIL 판정**: `MeasureType`에 따라 자동/수동 분기.
  - `NUMERIC`: 측정값을 `minValue/maxValue` 범위와 비교해 서버가 자동 판정.
  - `VISUAL`: 검사자가 직접 PASS/FAIL 입력.

### 6.3 LOT 상태 전이

```
PENDING ──(수입검사 전체 pass)──→ PASS    [InspectionService 자동]
PENDING ──(수입검사 1개라도 fail)─→ FAIL   [InspectionService 자동]
PENDING ──(관리자 수동)──────────→ HOLD   [PATCH /api/lots/{id}/status]
FAIL/HOLD ─(재검사 후)─────────→ PASS/FAIL [InspectionService 자동]
```

- 수동 전이는 `HOLD`로만 가능. PASS인 LOT는 HOLD로 못 돌림 (`Lot.holdManually`에서 차단).
- 재검사도 그냥 새 InspectionRecord 등록하면 됨 (이력은 모두 보존).

### 6.4 생산 등록 검증
- `ProductionLog` 생성 시 `lot.status == PASS`가 아니면 `LOT_NOT_PASSED` 예외.
- `defectQty > producedQty`면 `DEFECT_EXCEEDS_PRODUCED` 예외.
- 사업계획서의 "불량 LOT가 생산에 투입되는 것을 시스템적으로 차단"에 직접 대응.

### 6.5 검사기준(InspectionSpec) 버전 관리
- 수정/삭제 시 기존 spec은 삭제 안 하고 `supersededAt`만 채움 → 구버전 검사기록의 spec 참조 무결성 보존.
- 신규 spec은 `version` +1로 저장.
- 이력 조회 시 구버전도 로드 가능하도록 `findSpecEntityById`는 supersede 필터링 없음.

---

## 7. API 목록 (전체)

### Auth / User
| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | /api/auth/login | Public | JWT 발급 |
| GET | /api/users | 인증 | 목록 |
| POST | /api/users | ADMIN | 생성 |
| GET | /api/users/{id} | 인증 | 단건 |

### Material
| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | /api/materials | 인증 | 원자재 목록 |
| POST | /api/materials | ADMIN | 원자재 등록 |
| GET | /api/materials/{id} | 인증 | 단건 |
| DELETE | /api/materials/{id} | ADMIN | soft delete |
| GET | /api/materials/{id}/specs | 인증 | 원자재별 검사기준 |
| POST | /api/materials/specs | ADMIN | 검사기준 등록 |
| PUT | /api/materials/specs/{id} | ADMIN | 검사기준 개정(supersede + new) |
| DELETE | /api/materials/specs/{id} | ADMIN | 검사기준 삭제(supersede) |
| GET | /api/lots | 인증 | LOT 목록 |
| POST | /api/lots | 인증 | LOT 입고 등록 |
| GET | /api/lots/{id} | 인증 | LOT 단건 |
| **GET** | **/api/lots/{id}/history** | 인증 | **LOT 전체 이력 — 핵심 API** |
| PATCH | /api/lots/{id}/status | ADMIN | LOT 수동 HOLD 전이 |
| DELETE | /api/lots/{id} | ADMIN | soft delete |

### Inspection
| Method | Path | 권한 | 설명 |
|---|---|---|---|
| POST | /api/inspections | ADMIN, INSPECTOR | 수입검사 등록(자동 판정 + LOT 전이) |
| GET | /api/inspections/{id} | 인증 | 검사 단건 |
| GET | /api/lots/{lotId}/inspections | 인증 | LOT별 검사 이력 |

### Production
| Method | Path | 권한 | 설명 |
|---|---|---|---|
| GET | /api/equipment | 인증 | 설비 목록 |
| POST | /api/equipment | ADMIN | 설비 등록 |
| PATCH | /api/equipment/{id}/status | ADMIN, OPERATOR | 설비 상태 변경 |
| GET | /api/production-logs?todayOnly=bool | 인증 | 생산 이력 (당일 필터 옵션) |
| POST | /api/production-logs | ADMIN, OPERATOR | 생산 등록 (PASS LOT만) |
| GET | /api/alarms?activeOnly=bool | 인증 | 알람 목록 (미해소 필터 옵션) |
| POST | /api/alarms | ADMIN, OPERATOR | 알람 등록 |
| PATCH | /api/alarms/{id}/resolve | ADMIN, OPERATOR | 알람 해소 |
| GET | /api/dashboard | 인증 | 금일 생산량/활성 알람/설비 가동률/PENDING LOT 수 |

### Public Endpoints
`/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html`

---

## 8. 보안 (Spring Security)

- **Stateless**: 세션 없음, 모든 요청에 `Authorization: Bearer {token}` 필요.
- **JWT 구조** (`JwtProvider`):
  - subject = userId, claim = role
  - HS256 + 비밀키(`jwt.secret`, env override)
  - 만료 1시간 (`jwt.expiration-ms`)
- **Filter 체인**: `JwtAuthenticationFilter`가 `UsernamePasswordAuthenticationFilter` 앞에 위치.
  - 토큰 검증 → `MesPrincipal`을 `SecurityContext`에 주입.
- **권한 모델**: `UserRole = { ADMIN, INSPECTOR, OPERATOR }`
  - 메서드 단위 `@PreAuthorize("hasRole(...)") / hasAnyRole(...)` 사용 (`@EnableMethodSecurity`).
  - 마스터 데이터 변경 = ADMIN 전용
  - 수입검사 등록 = ADMIN + INSPECTOR
  - 생산 등록·알람 처리 = ADMIN + OPERATOR
- **인증 실패 응답**: `MesAuthenticationEntryPoint`가 401 + ApiResponse(error) JSON 반환.
- **비밀번호**: BCrypt 인코딩, 평문 저장 절대 없음.

---

## 9. 주요 ADR 요약 (질문 들어올 만한 결정들)

| # | 결정 | 이유 | 트레이드오프 |
|---|---|---|---|
| 001 | Spring Boot 4.0.6 | 회사 표준 | 일부 라이브러리 호환성 확인 필요 (레퍼런스가 3.x 위주) |
| 002 | Package by Feature | 도메인별 응집도, 수정 시 파악 범위 좁음 | 패키지 간 참조 규칙 명시 안 하면 순환 위험 |
| 003 | Enum을 도메인 패키지에 배치 | Package by Feature 원칙 준수 | cross-package import 발생 (단방향이면 허용) |
| 004 | Record DTO | 불변·간결, Bean Validation 가독성 | setter 없음, 상속 불가(DTO엔 불필요) |
| 005 | Service 간 패키지 경계 (Repository 직주입 금지) | 결합도 제한, 변경 영향 최소화 | 순환 위험 시 이벤트/별도 집계 서비스로 분리 |
| 006 | **AI 비전 검사 기능 제외** | 3차 고도화 범위, OJT 단계는 백엔드 기본 구조에 집중 | VisionInspection은 스켈레톤만 존재 |
| 007 | **Refresh Token 미구현** | OJT 범위 외, 만료 시 재로그인 | 운영 전환 시 추가 권장 |
| 008 | ddl-auto = create-drop | 개발 초기 스키마 변경 잦음 | 재시작 시 데이터 초기화, 운영 전 `validate`로 변경 필수 |
| 009 | Hexagonal 미적용 (Layered 유지) | OJT 목적엔 과도한 복잡도 | 테스트 시 Repository mock 처리 약간 번거로움 |

---

## 10. 사업계획서 vs 구현 범위 (스코프 정리)

### 1년차 영역 — 거의 모두 구현
| 사업계획서 요구 | 구현 상태 |
|---|---|
| 원자재 마스터 + LOT NO 관리 | 완료 |
| 수기 → 데이터화 (검사 항목별 KEY IN) | 완료 |
| 검사기준 데이터화 (버전 관리 포함) | 완료 |
| LOT별 입고→검사→생산 통합 이력 1회 조회 | 완료 (`GET /api/lots/{id}/history`) |
| 금일 생산량/알람/설비 가동률 실시간 집계 | 완료 (`/api/dashboard`) |
| 알람 이력 관리 | 완료 |
| PLC 신호 자동 수집 | 미구현 (설비 상태 수동 변경만) |
| 설비 제어 명령 송신 | 미구현 |
| 클라우드 + 로컬 DB 이중화/백업 | 미구현 (단일 PostgreSQL) |
| Cloud Web 사용자 화면 | 미구현 (Swagger UI만) |

### 2년차 영역 — 의도적 제외 (ADR-006)
| 요구 | 구현 상태 |
|---|---|
| AI 비전 외관/치수 검사 | VisionInspection 엔티티·컨트롤러 스켈레톤만 |
| 이미지 업로드/저장 | 미구현 (`imagePath` 필드만) |
| CNN/Segmentation/Boundary Detection | 미구현 |

### 3년차 영역 — 의도적 제외
| 요구 | 구현 상태 |
|---|---|
| 보드 IQC 전수검사 자동화 | InspectionSpec으로 표현은 가능, 자동화 인터페이스 없음 |
| ECG 시뮬레이터 / 시리얼 통신 | 미구현 |
| SMPS 전력량 인디게이터 통신 | 미구현 |
| AI 알고리즘 고도화 | 미구현 |

### 평가지표(사업계획서 표 2) 측정 가능 여부
- **5번 "원자재별 공정 이력 정확도 99 이상"** → LOT 이력 추적 백엔드로 측정 사정권 (단, 통계 산출 API는 아직 없음)
- 1~4번 (AI 정확도, 치수/타공홀 오차, 불량 판정) → AI 비전 미구현이므로 측정 불가
- 6~10번 (응답시간/동기화/F1/ROC/통신 오류율) → 인프라·모델·동기화 대상 자체가 없어 측정 불가

---

## 11. 한계 및 개선점

### 높음 — 사업계획서가 직접 요구하는데 비어 있음
1. ~~**InspectionSpec에 정량 판정 필드 없음**~~ → **✅ 해결**: `measureType(NUMERIC|VISUAL)`, `minValue`, `maxValue`, `unit` 필드 및 `judgeNumeric()` 자동판정 구현 완료.
2. **LOT 이력 정확도 통계 API 없음** (성과지표 5번 직결)
3. **이미지 업로드 골격 없음** — 사업계획서 표 4가 "이미지 데이터화"를 명시. 1차 단계에선 AI 없이 첨부 사진만 LOT에 묶어도 의미 있음.

### 중간 — 코드 품질/일관성
4. ~~**VisionInspectionController가 더미 200 OK 반환**~~ → **✅ 해결**: `HttpStatus.NOT_IMPLEMENTED`(501) 반환으로 수정됨.
5. **Enum 위치 불일치** — `user/UserRole`, `material/LotStatus`가 `enums/` 디렉토리 밖. CLAUDE.md 규칙 위반.
6. ~~**PRD.md 구현 현황 표가 코드 현실과 불일치**~~ → **✅ 해결**: Phase 1~4 완료, 시드 데이터·프론트엔드 현황 반영 완료.
7. **N+1 쿼리 가능성** (`InspectionService.getInspectionsByLot`)
   - record별로 detail 추가 조회. `findByRecord_IdIn` 배치 조회로 개선 권장 (LotHistoryService에는 이미 적용됨).
8. ~~**DataInitializer 시드 부족**~~ → **✅ 해결**: 원자재 6종·검사기준 20건·설비 4종·계정 3개 자동 생성 완료.

### 낮음 — 있으면 좋음
9. 단위 테스트 — 현재 `InspectionServiceTest`, `MaterialServiceTest`만. ProductionService(PASS LOT 검증), LotHistoryService, DashboardService 커버리지 필요.
10. Spring Boot Actuator 도입 (응답시간 측정 기반).
11. LOT 번호 생성 race condition — 운영 전환 시 DB 시퀀스 도입.

---

## 12. 예상 꼬리질문 Q&A

### Q1. "왜 AI 비전을 안 만들었나? 사업계획서 핵심이잖아."
→ ADR-006 결정. OJT 단계는 백엔드 기본 구조(레이어 분리, 트랜잭션, JPA 매핑, 인증/인가) 습득에 집중. AI 비전은 모델 학습·이미지 파이프라인·하드웨어(카메라·조명) 등 별도 트랙이라 신입 1인 단기 과제로 부적합. 다만 `VisionInspection` 엔티티를 미리 두어 3차 고도화 시 inspection 패키지에 Service·이미지 업로드 처리만 붙이면 되도록 진입로는 마련해둠.

### Q2. "LOT 상태 전이는 누가 결정하나? 검사자가 PASS/FAIL을 자기 손으로 누른다고?"
→ 맞음. 현재는 검사자가 항목별로 result를 직접 선택. 서버는 "FAIL 하나라도 있으면 전체 FAIL" 집계 규칙만 적용. **이게 한계이자 개선 1순위**. `InspectionSpec`에 `min/max/unit`이 들어가면 측정값으로 자동 판정 가능. 사업계획서 평가지표 1~3번(치수 오차 등) 측정에도 이게 선행조건.

### Q3. "PASS 아닌 LOT가 생산 라인에 들어가면?"
→ `POST /api/production-logs`에서 `lot.status != PASS`면 `LOT_NOT_PASSED` 예외(400). 서버 단에서 차단. 이 규칙이 InspectionServiceTest의 핵심 테스트 케이스.

### Q4. "트랜잭션 경계는?"
→ Service 클래스에 `@Transactional(readOnly = true)`를 기본으로 두고, 쓰기 메서드에만 `@Transactional` 명시. `InspectionService.createInspection`은 Record + N개 Detail + LOT 상태 변경을 단일 트랜잭션으로 묶어, Detail 저장 중 예외 시 Record까지 롤백.

### Q5. "JWT 만료 1시간인데 작업 중 끊기면?"
→ 재로그인 필요. Refresh Token 미구현(ADR-007). 운영 전환 시 Refresh + 토큰 회전 도입 예정. OJT 범위에선 단순화 목적.

### Q6. "DB는 왜 PostgreSQL? 사업계획서엔 클라우드·이중화 요구 있는데?"
→ 회사 표준 + JPA 호환성. 클라우드/이중화는 인프라 영역으로 OJT 백엔드 범위 외. 운영 전환 시 RDS + 리드 레플리카 또는 로컬-클라우드 동기화 큐 구조 검토.

### Q7. "ddl-auto: create-drop으로 운영 가능한가?"
→ 절대 불가. 개발 편의용. ADR-008에 명시된 대로 운영 전환 시 `validate`로 바꾸고 DDL은 Flyway/Liquibase로 관리. 현재 시점에선 운영 진입 전.

### Q8. "검사기준이 바뀌면 과거 검사 기록은 어떻게 되나?"
→ 검사기준은 **수정/삭제 시 supersede** 방식 (soft 개정). `superseded_at`이 채워지면 "구버전"으로 마킹되고, 기존 `InspectionDetail`의 FK는 구버전 spec을 그대로 가리킴. 신규 spec은 `version` +1로 별도 row 생성. 결과적으로 이력 무결성이 깨지지 않음.

### Q9. "원자재나 LOT 삭제는?"
→ Soft Delete. `deleted_at` 컬럼만 채움. 조회 메서드들이 `deletedAtIsNull` 필터링. 이력 보존이 의료기기 품질 추적의 핵심이라 hard delete는 절대 안 함.

### Q10. "LotHistoryService는 왜 다른 패키지 Repository를 직접 주입하나? Service 간 패키지 경계 규칙 위반 아닌가?"
→ ADR-005의 명시적 예외. 읽기 전용 집계 서비스에 한해 허용. 이걸 InspectionService·ProductionService를 거치게 만들면 N번의 cross-service 호출이 발생하고, 도메인 의미상 "집계"가 흐려짐. 단방향 읽기라 결합 부담도 낮음.

### Q11. "성능은 검증했나?"
→ 미검증. 현재까지는 기능 완성 단계. N+1 쿼리(특히 LotHistoryService)가 잠재 이슈로 파악됨. 운영 전환 전 Hibernate fetch 전략(`@EntityGraph` 또는 fetch join) 적용 + Actuator로 응답시간 측정 예정.

### Q12. "사업계획서 평가지표 중 지금 만족 가능한 게 있나?"
→ "원자재별 공정 이력 정확도 99 이상" 한 가지. 단, 입력 N건 대비 저장 N건 매칭 비율을 산출하는 통계 API가 아직 없어, 실제 평가 시 별도 쿼리/배치로 측정해야 함. 통계 API 추가가 짧은 작업이라 데모 전 우선 보강 권장.

### Q13. "테스트 커버리지는?"
→ Service 단위 테스트 2개(InspectionService, MaterialService). 핵심 시나리오는 InspectionService(LOT 상태 전이, spec-material 검증)가 가장 두텁고, 나머지(ProductionService의 PASS LOT 검증, DashboardService 집계, LotHistoryService 통합 조회)는 보강 필요. CLAUDE.md의 "반드시 테스트" 항목과 일부 갭 있음.

### Q14. "왜 백엔드만 만들고 프론트는 없나?"
→ PRD에서 명시적으로 MVP 제외. 백엔드 API + Swagger UI로 검증. 사업계획서의 "Cloud Web"은 회사 R&D 과제 전체 범위라 별도 프론트 트랙에서 진행하거나 시제품 단계에서 통합.

### Q15. "사업계획서 원자재(바이오센서/심전계)와 코드 원자재(펫패치/Main Board/배터리/LCD)가 다른데?"
→ 사업계획서는 제품 카테고리 레벨 표현이고, 실제 회사 라인업의 원자재 매핑은 코드 쪽이 맞음(추정). PRD에 매핑 관계를 한 줄 명시해 평가자가 혼동하지 않도록 보강 필요. 만약 매핑이 틀린 거면 RawMaterial 시드 데이터 자체를 사업계획서 라인업으로 맞추는 게 정공법.

---

## 13. 실행 명령

```powershell
.\gradlew.bat clean build      # 빌드
.\gradlew.bat test             # 테스트
.\gradlew.bat bootRun          # 실행 (localhost:8080)
```

- 환경변수: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`
- 초기 계정 (DataInitializer 자동 생성):
  - `admin / admin123` (ADMIN)
  - `inspector / inspector123` (INSPECTOR)
  - `operator / operator123` (OPERATOR)
- 시드 데이터: 원자재 6종(RM-001~006) + 검사기준 20건 + 설비 4종 자동 등록
- Swagger: `http://localhost:8080/swagger-ui.html`

---

## 14. 정리 — 한 페이지로 설명할 때

> "**의료장비 원자재 수입검사를 종이에서 DB로 옮기고, LOT 번호 하나로 입고부터 생산까지 추적하는 Spring Boot 백엔드**예요.
> 핵심 API는 `GET /api/lots/{id}/history` 하나로 그 LOT의 입고·검사 항목별 실측값·생산 이력을 한 번에 반환하는 거고요.
> 수입검사를 등록하면 Service가 자동으로 LOT 상태를 PASS/FAIL로 전이시키고, PASS 아닌 LOT는 생산 라인 등록 자체를 막아요.
> NUMERIC 검사기준은 측정값을 minValue/maxValue 범위와 비교해 서버가 자동 판정하고, VISUAL 검사는 검사자가 직접 PASS/FAIL을 선택해요.
> AI 비전·클라우드 이중화·PLC 연동은 R&D 사업계획서 2~3년차 영역이라 OJT 범위에서 의도적으로 제외했고, ADR에 결정 근거를 남겨놨어요.
> 앱 실행 시 OJT 기준서의 원자재 6종·검사기준 20건·설비 4종이 자동 시드되어 별도 등록 없이 바로 검사 등록까지 시연할 수 있어요."
