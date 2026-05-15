# medmes

중소기업 의료장비 원자재 수입검사 디지털화 및 LOT 이력 추적 백엔드. 수기 종이 기록을 DB로 전환하고 원자재 입고→검사→생산 전 과정을 LOT 번호 하나로 추적한다.

## Stack

* **Runtime**: Java 17 / Spring Boot 4.x / Gradle
* **DB**: PostgreSQL / Spring Data JPA / `ddl-auto: create-drop` (개발), `validate` (운영)
* **Auth**: Spring Security Stateless / JWT access token (jjwt 0.12.x)
* **기타**: Lombok / Record DTO / springdoc-openapi / Bean Validation

## Modules

* `global`: ApiResponse, ErrorCode, BusinessException, GlobalExceptionHandler, JWT (JwtProvider, JwtFilter, SecurityConfig, MesPrincipal)
* `auth`: 로그인 API (`POST /api/auth/login`), JWT 발급
* `user`: 사용자 CRUD, BCrypt 비밀번호
* `material`: 원자재 마스터, LOT 입고 등록, 검사기준 등록, LOT 이력 집계 (LotHistoryService)
* `inspection`: 수입검사 등록 — **핵심 비즈니스 로직** (자동 판정 + LOT 상태 전이)
* `production`: 설비 관리, 생산 이력 (PASS LOT만 허용), 알람 로그

## Docs

* `docs/ARCHITECTURE.md`: 패키지 구조, 요청·응답 흐름, 도메인 모델, API 목록, 비즈니스 규칙
* `docs/PRD.md`: 제품 목표, 사용자, 구현 현황, 로드맵
* `docs/ADR.md`: 기술 결정 기록 (선택 / 이유 / 트레이드오프)

## Principles

* 한 도메인 수직 슬라이스를 끝까지 완성한 후 다음으로 이동
* 결정이 필요하면 `docs/ARCHITECTURE.md` 확인 → 없으면 `docs/ADR.md`에 추가
* 새 도메인·API 변경·규칙 변경 시 관련 docs도 함께 업데이트

## Architecture Rules

**요청 흐름**
```
HTTP Request → Controller (@Valid) → Service (@Transactional) → Repository → DB
                                          ↓
                                   Entity ↔ Record DTO 변환
                                          ↓
                              ResponseEntity<ApiResponse<T>>
```

**레이어 경계**
* Service는 다른 패키지의 Repository를 직접 주입하지 않는다 — 필요하면 해당 패키지 Service를 통한다
* 예외: `LotHistoryService`는 읽기 전용 집계 서비스로 여러 패키지 Repository 주입 허용
* Controller는 Entity를 응답에 직접 노출하지 않는다 — 항상 Record DTO로 변환

**패키지 간 의존 방향**
```
global ← user ← auth
global ← material
global ← inspection → material, user
global ← production → material
material (LotHistoryService) → inspection, production (읽기 전용)
```

**LOT 상태 전이**
```
PENDING → PASS/FAIL : 수입검사 완료 시 InspectionService가 자동 처리
PENDING → HOLD     : 관리자 수동 (PATCH /api/lots/{id}/status)
FAIL/HOLD → PASS/FAIL : 재검사 후
```
PASS가 아닌 LOT에 생산 이력 등록 시 → `LOT_NOT_PASSED` 예외

## Enum 위치

각 Enum은 해당 도메인 패키지 `enums/` 디렉토리에 위치한다.

| Enum | 패키지 |
|------|--------|
| `LotStatus` | `material/enums/` |
| `InspectionResult` | `inspection/enums/` |
| `UserRole` | `user/enums/` |
| `AlarmSeverity`, `EquipmentStatus` | `production/enums/` |

## Comment Style

* 한 줄 주석만, 한국어로 작성
* 코드만으로 의도가 불명확하거나 숨겨진 제약이 있을 때만 작성

## Security Rules

* JWT secret, DB 비밀번호 커밋 금지 — `${ENV_VAR:default}` 형식 사용
* Request Record에 Bean Validation 필수
* 공개 엔드포인트: `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`

## Testing

* 우선순위: Service 단위 테스트 → Repository `@DataJpaTest` → Controller `@WebMvcTest`
* 반드시 테스트: 수입검사 등록 시 LOT 상태 전이 (PASS/FAIL), 트랜잭션 롤백, 중복 LOT 번호
* 과도한 mock 지양 — 핵심 시나리오(성공/실패)만

## Roadmap

| Phase | 내용 | 상태 |
|---|---|---|
| 0 | 프로젝트 세팅, global / auth / user | ✅ |
| 1 | material 도메인 (원자재, LOT, 검사기준) | ✅ |
| 2 | inspection 도메인 (수입검사 자동 판정) | ✅ |
| 3 | production 도메인 (설비, 생산, 알람) | ✅ |
| 4 | LotHistoryService 완성 + Service 단위 테스트 | ✅ |
| 5 | 운영 준비 (ddl validate, 시드 데이터, 배포 설정) | 🔄 시드 완료 |

## Common Commands

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
.\gradlew.bat bootRun
```

---

# Claude Code 행동 가이드라인

## 1. 코딩 전 생각 먼저

* 가정을 명시적으로 말한다. 불확실하면 질문한다.
* 여러 해석이 가능하면 제시한다 — 조용히 선택하지 않는다.
* 더 단순한 방법이 있으면 먼저 말한다.

## 2. 단순성 우선

* 요청한 것만 구현한다. 추측성 기능 추가 금지.
* 단일 사용 코드에 추상화 금지.
* 불가능한 시나리오에 대한 에러 핸들링 금지.

## 3. 수술적 변경

* 필요한 것만 수정한다. 인접 코드 개선 금지.
* 깨지지 않은 것을 리팩토링하지 않는다.
* 내 변경으로 생긴 고아(import/변수/함수)만 제거한다.

## 4. 목표 기반 실행

* 멀티스텝 작업은 계획을 먼저 제시한다.
* 검증 기준을 정의하고 충족될 때까지 반복한다.