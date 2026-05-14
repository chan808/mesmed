# MedMES 기술 정리 — 담당자 질의 대비

> 의료장비 원자재 수입검사 디지털화 및 LOT 이력 추적 백엔드  
> Java 17 / Spring Boot / PostgreSQL / JWT  

---

## 목차

1. [프로젝트 목적과 범위](#1-프로젝트-목적과-범위)
2. [기술 스택 선택 이유](#2-기술-스택-선택-이유)
3. [아키텍처 설계](#3-아키텍처-설계)
4. [인증 — JWT 구현 상세](#4-인증--jwt-구현-상세)
5. [도메인별 비즈니스 로직](#5-도메인별-비즈니스-로직)
6. [핵심 설계 결정과 트레이드오프](#6-핵심-설계-결정과-트레이드오프)
7. [알려진 한계와 개선 방향](#7-알려진-한계와-개선-방향)

---

## 1. 프로젝트 목적과 범위

### 해결하는 문제

중소 의료기기 제조사는 원자재 수입검사를 종이 검사표로 운영한다. 이로 인해 세 가지 문제가 발생한다.

- **감사 대응 지연**: 식약처 GMP 감사 시 특정 LOT 이력을 수기 문서에서 찾는 데 수 시간이 걸린다.
- **불량 원자재 투입 위험**: 검사 실패한 LOT가 담당자 실수로 생산 라인에 투입될 수 있다.
- **데이터 단절**: 입고·검사·생산이 각각 다른 문서에 기록돼 LOT 하나의 전체 이력을 한눈에 볼 수 없다.

### 구현 범위

| 도메인 | 핵심 기능 |
|--------|-----------|
| material | 원자재 마스터, 검사기준(버전 관리), LOT 입고 |
| inspection | 수입검사 등록, PASS/FAIL 자동 판정, LOT 상태 전이 |
| production | 설비 관리, 생산 이력 기록, 알람 관리 |
| global | JWT 인증, 공통 예외 처리, LOT 이력 집계 |

---

## 2. 기술 스택 선택 이유

### Java 17 + Spring Boot

Spring은 의료·제조 분야 엔터프라이즈 시스템에서 사실상 표준이다. OJT 목적상 실무 환경과 동일한 스택을 선택했다. Java 17은 Record 타입(DTO 간결화)과 sealed class를 제공하며, LTS 버전이라 유지보수가 보장된다.

**트레이드오프**: Spring의 무거운 초기 설정 비용이 있다. 단순 CRUD만 필요했다면 Ktor나 FastAPI가 더 빠를 수 있다. 그러나 트랜잭션 관리, JPA 통합, Security 필터 체인 등 엔터프라이즈 기능을 수작업 없이 쓰려면 Spring이 적합하다.

### PostgreSQL

- JSON 컬럼 지원이 향후 비정형 검사 데이터 저장에 유리하다.
- `ENUM` 타입, 배열 타입 등 표준 SQL을 넘는 기능을 제공한다.
- 무료이면서 트랜잭션 안정성(ACID)이 높다.

**트레이드오프**: MySQL 대비 초기 설정이 복잡하고 운영 도구가 적다. 소규모 팀에서는 MySQL이 더 친숙할 수 있다. 하지만 의료기기 데이터의 정합성 요구 수준을 감안하면 PostgreSQL의 엄격한 타입 시스템이 유리하다.

### Spring Data JPA (Hibernate)

SQL을 직접 작성하지 않고 객체 중심으로 도메인 모델을 표현할 수 있다. 복잡한 집계 쿼리가 적고 도메인 규칙이 중요한 이 프로젝트에 적합하다.

**트레이드오프**: 복잡한 통계 쿼리나 대량 배치는 JPA보다 MyBatis나 JPQL이 유리하다. `LotHistoryService`처럼 여러 테이블을 조인하는 경우 N+1 문제에 주의해야 한다(후술).

### jjwt 0.12.x

최신 JJWT API를 사용해 deprecated된 `.setSubject()` 대신 `.subject()`를 쓴다. 서명 알고리즘을 명시하지 않아도 키 길이에 따라 자동 선택(HS256/HS384/HS512)된다.

---

## 3. 아키텍처 설계

### 요청 흐름

```
HTTP Request
  → JwtAuthenticationFilter (토큰 파싱, SecurityContext 설정)
  → Controller (@Valid 입력 검증)
  → Service (@Transactional 비즈니스 로직)
  → Repository (DB 접근)
  → Entity (상태 변경 메서드)
  → ResponseEntity<ApiResponse<T>> 반환
```

### 패키지 간 의존 방향

```
global ← material
global ← inspection → material (MaterialService 통해서만)
global ← production → material (MaterialService 통해서만)
material (LotHistoryService) → inspection, production (읽기 전용)
```

**핵심 규칙: Service는 다른 패키지의 Repository를 직접 주입하지 않는다.**

예를 들어 `ProductionService`가 LOT를 조회할 때 `LotRepository`를 직접 주입하면 두 모듈 사이에 강한 결합이 생긴다. LOT 조회 로직이 바뀔 때 `ProductionService`도 함께 수정해야 한다. `MaterialService.findLotEntityById()`를 통하면 LOT 관련 비즈니스 규칙(삭제 여부 확인 등)이 한 곳에서만 관리된다.

**예외**: `LotHistoryService`는 여러 도메인의 Repository를 직접 주입한다. 이 서비스는 **읽기 전용 집계** 전용으로, 상태를 변경하지 않으며 여러 도메인 데이터를 한 트랜잭션에서 묶어야 하는 특수한 목적을 갖는다.

### 레이어별 책임

| 레이어 | 책임 | 하지 않는 것 |
|--------|------|-------------|
| Controller | HTTP 매핑, `@Valid` 입력 검증 | 비즈니스 로직, Entity 직접 노출 |
| Service | 트랜잭션, 비즈니스 규칙, 예외 처리 | HTTP 관련 코드 |
| Entity | 상태 변경 메서드 (`softDelete`, `holdManually` 등) | DB 접근, 외부 서비스 호출 |
| Repository | DB 쿼리 | 비즈니스 로직 |

### Record DTO 선택 이유

Java 16부터 정식 도입된 `record`는 불변 객체를 간결하게 선언한다.

```java
// record: 4줄
public record LotRequest(
    @NotNull Long rawMaterialId,
    Integer quantity
) {}

// 일반 class: ~20줄 (필드, 생성자, getter, equals, hashCode, toString)
```

DTO는 데이터 운반체이므로 불변이어야 한다. Record는 setter가 없어 Controller 계층에서 Service로 넘어가는 도중 값이 변조될 수 없다.

---

## 4. 인증 — JWT 구현 상세

### 세션 방식이 아닌 JWT를 선택한 이유

MES 시스템은 향후 모바일 앱, 외부 SCADA 시스템과 연동될 가능성이 있다. 세션 방식은 서버가 세션 저장소(메모리 또는 Redis)를 유지해야 하므로 수평 확장 시 세션 동기화 문제가 생긴다. JWT는 Stateless이므로 서버가 어떤 인스턴스에 요청을 받아도 토큰만 검증하면 된다.

**트레이드오프**: JWT는 한 번 발급하면 만료 전 강제 무효화가 불가능하다. 사용자 계정 탈취 시 토큰을 즉시 무효화할 수 없다. 이를 해결하려면 토큰 블랙리스트(Redis 등)가 필요한데 그러면 Stateless의 장점이 줄어든다. 현재 구현은 토큰 만료 시간(1시간)으로 위험을 제한하고 있다.

### 토큰 구조

```
Header: { alg: HS256, typ: JWT }
Payload: { sub: "userId", role: "ADMIN", iat: ..., exp: ... }
Signature: HMAC-SHA256(header.payload, secretKey)
```

`sub`에 userId를 넣어 DB 조회 없이 요청자 신원을 식별한다. `role`을 Payload에 포함해 권한 확인 시 DB 조회를 줄인다.

### HMAC-SHA256 (대칭키) 선택 이유

RS256(비대칭키) 대비 구현이 단순하고 성능이 빠르다. RS256은 외부 서비스가 토큰을 검증해야 하는 경우(공개키 배포가 필요한 MSA 환경)에 적합하다. 단일 서버 구조에서는 HS256으로 충분하다.

**트레이드오프**: 서명 키가 유출되면 누구든 유효한 토큰을 만들 수 있다. `${JWT_SECRET:default}` 형식으로 환경변수에서 주입하고, 프로덕션에서는 최소 32바이트의 랜덤 키를 사용해야 한다.

### 필터 흐름 (`JwtAuthenticationFilter`)

```java
// OncePerRequestFilter — 요청당 정확히 1번만 실행 보장
String token = extractToken(request);  // "Bearer " 이후 추출

if (token != null) {
    try {
        MesPrincipal principal = jwtProvider.parseToken(token);
        // UsernamePasswordAuthenticationToken으로 래핑 → SecurityContext 저장
        SecurityContextHolder.getContext().setAuthentication(auth);
    } catch (Exception e) {
        SecurityContextHolder.clearContext();  // 파싱 실패 시 인증 없음으로 처리
    }
}
chain.doFilter(request, response);  // 토큰이 없어도 필터 체인 계속 진행
```

토큰이 없거나 잘못된 토큰은 예외를 던지지 않고 인증 없는 상태로 체인을 통과시킨다. 이후 `SecurityConfig`의 `.anyRequest().authenticated()` 규칙에서 `MesAuthenticationEntryPoint`가 `401`을 반환한다. 필터에서 직접 응답하지 않기 때문에 필터와 보안 정책이 분리된다.

### `MesPrincipal` (Record)

```java
public record MesPrincipal(Long userId, String role) {}
```

Spring Security의 `UserDetails`를 구현하지 않고 단순 Record를 사용했다. `UserDetails`는 계정 잠금, 만료 등 여러 기능을 포함하는데 현재 요구사항에 불필요하다. `Authentication.getPrincipal()`로 꺼낼 수 있고 필요한 정보(userId, role)만 담는다.

### 공개 엔드포인트

```
/api/auth/**       — 로그인 (토큰 없이 접근)
/swagger-ui/**     — API 문서
/v3/api-docs/**    — OpenAPI 스펙
```

그 외 모든 엔드포인트는 유효한 JWT 필요.

### 비밀번호 저장 — BCrypt

BCrypt는 단방향 해시에 **salt**를 자동으로 포함시켜 같은 비밀번호도 매번 다른 해시값을 만든다. Rainbow table 공격을 막는다. work factor(기본 10)로 해시 연산 비용을 조절해 브루트포스를 느리게 만든다.

**트레이드오프**: Argon2id가 현재 더 권장되지만, Spring Security가 기본 제공하는 BCrypt로도 충분한 보안 수준을 제공한다.

---

## 5. 도메인별 비즈니스 로직

### 5-1. material — 원자재, 검사기준, LOT

#### RawMaterial (원자재 마스터)

원자재는 `code` 컬럼에 유니크 제약이 있다. `existsByCode()`로 저장 전에 먼저 검사한다. DB 제약만으로도 중복을 막을 수 있지만, DB 예외는 그대로 올라오면 500이 된다. 서비스 단에서 미리 검사해 `409 Conflict`를 명시적으로 반환한다.

**Soft Delete**: `deleted_at` 타임스탬프를 기록한다. `IS_ACTIVE` boolean 대신 타임스탬프를 쓴 이유는 "언제 삭제됐는지"까지 기록하기 위해서다. 삭제된 원자재는 `findAllByDeletedAtIsNull()`로 자동 제외된다. 원자재 코드(예: `SUS-304`)는 삭제 후에도 재사용을 막는다 — `existsByCode()`는 deleted_at 필터 없이 전체 조회.

#### InspectionSpec (검사기준) — 버전 관리

```
검사기준이 바뀌는 상황: "직경 공차 기준이 ±0.1mm → ±0.08mm로 강화됨"
```

이전 기준을 덮어쓰면 이전 기준으로 수행된 과거 검사 기록의 근거가 사라진다. GMP 감사에서 "당시 기준이 무엇이었느냐"를 소명해야 하므로 기준의 이력 보존이 필수다.

**구현 방식**: `superseded_at` 컬럼(nullable). `null`이면 현행 기준, `not null`이면 대체된 구버전.

```
PATCH /api/materials/specs/{id} 호출 시:
  1. 기존 spec.supersede()  → superseded_at = now()
  2. 새 spec 생성           → version = 기존 + 1
```

**InspectionDetail은 구버전 spec을 참조 가능**: `findSpecEntityById()`는 `superseded_at` 필터 없이 조회한다. 과거 검사 기록이 당시 적용된 기준 버전을 그대로 가리키도록 보장한다.

**트레이드오프**: 별도 `InspectionSpecRevision` 테이블로 이력을 분리하는 방법도 있다. 그러면 현행 spec 테이블이 깔끔해지지만 쿼리가 복잡해진다. 현재 방식은 단일 테이블에서 `superseded_at IS NULL` 조건으로 현행 버전을 조회하므로 단순하다.

#### Lot (입고 LOT) — 상태 머신

```
PENDING (입고 대기)
  → PASS / FAIL  : 수입검사 완료 시 InspectionService가 자동 처리
  → HOLD         : 관리자 수동 (PATCH /api/lots/{id}/status)
FAIL / HOLD
  → PASS / FAIL  : 재검사 후 자동 처리
PASS
  → HOLD         : 불가 (이미 생산 가능 상태)
```

상태 전이 규칙을 **Entity 메서드**에 넣은 이유: 서비스 레이어에 if-else로 흩뿌리면 새 서비스가 추가될 때 규칙을 빠뜨릴 수 있다. Entity에 있으면 어떤 경로로 호출해도 규칙이 항상 적용된다.

```java
// 수동 전이는 HOLD만 허용
public void holdManually() {
    if (this.status == LotStatus.PASS) {
        throw new BusinessException(MaterialErrorCode.INVALID_STATUS_TRANSITION);
    }
    this.status = LotStatus.HOLD;
}

// 검사 완료 시 자동 전이 (제약 없음)
public void applyInspectionResult(LotStatus result) {
    this.status = result;
}
```

두 메서드를 분리한 이유: 수동 API와 자동 판정은 허용 범위가 다르다. 하나의 메서드로 합치면 수동 API에서도 PASS 직접 변경이 가능해진다.

**LOT 번호 자동 생성**: `LOT-YYYYMMDD-NNN` 형식. `countByLotNoStartingWith(prefix) + 1`로 시퀀스를 만든다. **동시성 취약점**: 같은 시점 요청 두 건이 같은 번호를 계산하면 DB 유니크 제약 위반으로 500이 난다. 운영 환경에서는 DB 시퀀스(`SERIAL`) 또는 분산 ID 생성기(Snowflake)로 교체해야 한다.

**Soft Delete**: LOT 삭제 시 `deleted_at`만 기록. 연결된 InspectionRecord, ProductionLog는 삭제하지 않는다. 이력 보존이 법적 요건이기 때문이다.

### 5-2. inspection — 수입검사

#### 자동 합격 판정 로직

```java
boolean anyFail = details.stream()
        .anyMatch(d -> d.getResult() == InspectionResult.FAIL);
InspectionResult overall = anyFail ? InspectionResult.FAIL : InspectionResult.PASS;
```

"하나라도 FAIL이면 전체 FAIL" 규칙. 의료기기 원자재는 모든 검사 항목을 통과해야 사용 가능하다. AND 조건이다.

**트레이드오프**: 동료 프로젝트처럼 `INSPECTION_SEVERITY(치명/주요/경미)`와 `AQL` 샘플링을 조합하면 "경미 항목은 일정 기준 이하 불량 허용"같은 조건부 합격이 가능하다. 현재는 단순 AND 조건이다. 정밀도가 요구되는 의료기기에서는 이 단순 규칙이 오히려 더 안전하다.

#### InspectionSpec ↔ LOT 원자재 일치 검증

```java
Long lotMaterialId = record.getLot().getRawMaterial().getId();
if (!spec.getRawMaterial().getId().equals(lotMaterialId)) {
    throw new BusinessException(InspectionErrorCode.SPEC_MATERIAL_MISMATCH);
}
```

LOT의 원자재와 다른 원자재의 검사기준 ID를 제출하는 것을 차단한다. 이 검증이 없으면 "스테인리스 봉재 LOT에 실리콘 시트 검사기준을 적용"하는 오류 데이터가 저장될 수 있다.

#### InspectionSeverity (결함 심각도)

```
CRITICAL — 안전·성능에 직결, 즉시 사용 불가
MAJOR    — 기능 영향, 재검토 필요
MINOR    — 외관·규격 미세 이탈, 조건부 허용 가능
```

현재는 기록 목적으로만 사용한다(현행 합격 판정에는 영향 없음). 향후 "MINOR 항목은 2개까지 허용"같은 조건부 합격 규칙 추가 시 이 필드를 판정 로직에 연결할 수 있다.

#### 트랜잭션 경계

```java
@Transactional
public InspectionResponse createInspection(InspectionRequest request) {
    // 1. LOT 조회
    // 2. InspectionRecord 저장
    // 3. InspectionDetail 저장 (N건)
    // 4. 합격 판정 계산
    // 5. record.conclude(overall)
    // 6. lot.applyInspectionResult(...)  ← LOT 상태 전이
}
```

1~6이 하나의 트랜잭션이다. 3단계에서 특정 Detail 저장이 실패하면 LOT 상태 전이도 함께 롤백된다. 부분 저장이 발생하지 않는다.

### 5-3. production — 설비·생산·알람

#### PASS LOT만 생산 투입 허용

```java
if (lot.getStatus() != LotStatus.PASS) {
    throw new BusinessException(ProductionErrorCode.LOT_NOT_PASSED);
}
```

이 한 줄이 시스템의 핵심 안전망이다. 불량 원자재가 제품에 들어가는 것을 **소프트웨어 레벨**에서 원천 차단한다.

#### defectQty <= producedQty 검증

```java
if (request.defectQty() != null && request.defectQty() > request.producedQty()) {
    throw new BusinessException(ProductionErrorCode.DEFECT_EXCEEDS_PRODUCED);
}
```

Bean Validation의 `@AssertTrue`로 DTO에 넣는 방법도 있다. 서비스에 넣은 이유는 두 필드 간 관계 검증이기 때문이다. `@AssertTrue`는 단일 필드 제약에 적합하고, 복합 필드 규칙은 서비스나 커스텀 Validator가 더 명확하다.

#### 알람 처리

```java
// 해소 멱등성 보호
if (alarm.isResolved()) {
    throw new BusinessException(ProductionErrorCode.ALARM_ALREADY_RESOLVED);
}
alarm.resolve();  // resolvedAt = now()
```

이미 해소된 알람을 다시 해소하려 하면 `400`을 반환한다. 의도치 않은 중복 API 호출에 대한 방어다.

### 5-4. LotHistoryService — LOT 이력 집계

```
GET /api/lots/{id}/history
→ LOT 기본 정보
→ 검사 이력 전체 (재검사 포함)
→ 생산 이력 전체
```

**여러 도메인의 Repository를 직접 주입하는 이유**: 이 서비스는 읽기 전용 집계 전용이다. 상태를 변경하지 않으므로 서비스 간 순환 의존성 위험이 없다. `@Transactional(readOnly = true)`로 선언해 Hibernate 쓰기 지연(Dirty Checking)을 비활성화해 성능을 높인다.

---

## 6. 핵심 설계 결정과 트레이드오프

### 6-1. `ddl-auto: create-drop` vs `validate`

| 설정 | 동작 | 사용 시점 |
|------|------|-----------|
| `create-drop` | 앱 시작 시 스키마 생성, 종료 시 삭제 | 개발 중 (현재) |
| `validate` | 엔티티와 스키마 불일치 시 시작 실패 | 운영 |
| `none` | JPA가 DDL에 관여 안 함 | Flyway/Liquibase 사용 시 |

현재 `create-drop`은 개발 편의를 위한 것이다. 운영 전환 시 `validate`로 바꾸고 `schema.sql` 마이그레이션 스크립트를 작성해야 한다.

### 6-2. Soft Delete — `deleted_at` vs `is_active`

| 방식 | 장점 | 단점 |
|------|------|------|
| `deleted_at` (현재) | 삭제 시각 기록, 감사 추적 가능 | 쿼리에 `IS NULL` 조건 필요 |
| `is_active` | 쿼리 조건이 직관적 | 삭제 시각 정보 없음 |

의료기기 데이터는 언제 삭제됐는지가 감사 항목이 될 수 있어 `deleted_at`이 더 적합하다.

`@SQLRestriction("deleted_at IS NULL")`을 엔티티에 붙이면 모든 쿼리에 자동으로 조건이 적용된다. 현재 구현에서 이를 사용하지 않은 이유: `InspectionDetail.spec`처럼 다른 엔티티가 Lazy 로딩으로 참조하는 경우, 해당 spec이 supersede된 구버전이면 로딩 시 제약 조건에 걸려 예외가 발생한다. 명시적 Repository 메서드(`findAllByDeletedAtIsNull`)로 제어하는 방식이 더 안전하다.

### 6-3. ErrorCode 인터페이스 설계

```java
public interface ErrorCode {
    int getStatus();
    String getMessage();
}
```

각 도메인이 `ErrorCode`를 구현하는 enum을 가진다(`MaterialErrorCode`, `InspectionErrorCode`, ...). HTTP 상태 코드와 메시지를 에러 코드에 묶어두면:

- `GlobalExceptionHandler`가 도메인을 몰라도 `e.getErrorCode().getStatus()`로 응답 상태를 결정할 수 있다.
- 새 도메인 추가 시 `GlobalExceptionHandler` 수정 없이 도메인 enum만 추가하면 된다.

**트레이드오프**: HTTP 상태 코드가 비즈니스 코드에 들어가는 것이 계층 오염이라는 시각도 있다. 도메인이 HTTP를 알아서는 안 된다는 관점에서 에러 코드와 HTTP 상태 매핑 테이블을 별도로 두는 방법도 있다. 현재 규모에서는 단순성 우선으로 현 구조를 유지한다.

### 6-4. N+1 쿼리 문제

`getInspectionsByLot()`는 N+1 패턴이다:

```java
// 검사 기록 1 쿼리
return recordRepository.findByLotId(lotId).stream()
    .map(record -> InspectionResponse.from(
        record,
        // 기록마다 Detail 쿼리 1번 → N쿼리
        detailRepository.findByRecord_Id(record.getId())))
    .toList();
```

재검사가 없는 일반적인 경우 기록이 1~2건이므로 현재는 큰 문제가 없다. 데이터가 늘면 `@EntityGraph`나 JPQL JOIN FETCH로 교체해야 한다.

### 6-5. 페이지네이션 미적용

`getAllLots()`, `getAllProductionLogs()` 등 전체 목록 API에 페이지네이션이 없다. 운영 데이터가 수천 건을 넘으면 메모리 이슈와 응답 시간 저하가 발생한다. `Pageable`을 파라미터로 받는 `Page<T>` 반환으로 전환이 필요하다. 현재는 OJT 범위에서 생략했다.

---

## 7. 알려진 한계와 개선 방향

### 단기 개선 필요

| 문제 | 현재 상태 | 개선 방법 |
|------|-----------|-----------|
| LOT 번호 동시성 | `count + 1` 방식으로 race condition | DB SEQUENCE 또는 UUID |
| 페이지네이션 없음 | 전체 목록 반환 | `Pageable` + `Page<T>` |
| JWT 리프레시 없음 | 1시간 만료 시 재로그인 필요 | Refresh Token 도입 |
| 역할 기반 접근 제어 없음 | 모든 인증 사용자가 모든 API 접근 | `@PreAuthorize("hasRole('ADMIN')")` 추가 |

### 운영 전환 시 필수

| 항목 | 내용 |
|------|------|
| `ddl-auto: validate` | `schema.sql` 마이그레이션 스크립트 작성 |
| 비밀키 관리 | JWT_SECRET, DB_PASSWORD를 Vault 또는 K8s Secret으로 |
| 로깅 | 검사 판정, LOT 상태 전이 이벤트 감사 로그 |
| 서비스 단위 테스트 | InspectionService 판정 로직, LOT 상태 전이 |
| AQL 샘플링 | ISO 2859 기반 로트 크기별 샘플 수·합격 기준 |

### 동료 프로젝트 대비 차이점

동료 프로젝트는 **검사기준서(문서)** 관리에 집중했다. AQL 샘플링, 제품/모델/공급사 마스터, 개정이력 관리가 강점이다. 이 프로젝트는 **공정 흐름(LOT 추적)** 에 집중했다. LOT 상태 머신, 생산 투입 통제, 전 공정 단일 이력 조회가 강점이다. 실제 MES는 두 영역이 모두 필요하며 통합됐을 때 완전한 시스템이 된다.

---

## API 목록 요약

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/auth/login` | 로그인, JWT 발급 |
| POST | `/api/materials` | 원자재 등록 |
| GET | `/api/materials` | 원자재 목록 |
| GET | `/api/materials/{id}` | 원자재 단건 |
| DELETE | `/api/materials/{id}` | 원자재 소프트딜리트 |
| GET | `/api/materials/{id}/specs` | 현행 검사기준 목록 |
| POST | `/api/materials/specs` | 검사기준 등록 |
| PATCH | `/api/materials/specs/{id}` | 검사기준 개정 (새 버전 생성) |
| DELETE | `/api/materials/specs/{id}` | 검사기준 비활성화 |
| POST | `/api/lots` | LOT 입고 등록 |
| GET | `/api/lots` | LOT 목록 |
| GET | `/api/lots/{id}` | LOT 단건 |
| DELETE | `/api/lots/{id}` | LOT 소프트딜리트 |
| PATCH | `/api/lots/{id}/status` | LOT 상태 수동 변경 (HOLD만 허용) |
| GET | `/api/lots/{id}/history` | LOT 전체 이력 (입고+검사+생산) |
| GET | `/api/lots/{lotId}/inspections` | LOT별 검사 이력 |
| POST | `/api/inspections` | 수입검사 등록 (자동 판정) |
| GET | `/api/inspections/{id}` | 검사 단건 |
| POST | `/api/equipment` | 설비 등록 |
| GET | `/api/equipment` | 설비 목록 |
| PATCH | `/api/equipment/{id}/status` | 설비 상태 변경 |
| POST | `/api/production-logs` | 생산 이력 등록 |
| GET | `/api/production-logs` | 생산 이력 목록 (`?todayOnly=true`) |
| POST | `/api/alarms` | 알람 등록 |
| GET | `/api/alarms` | 알람 목록 (`?activeOnly=true`) |
| PATCH | `/api/alarms/{id}/resolve` | 알람 해소 |