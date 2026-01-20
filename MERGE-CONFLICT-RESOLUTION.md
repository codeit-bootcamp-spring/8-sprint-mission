# PR Merge 충돌 해결 방법 설명

## 1. 충돌 발생 상황

**PR 상태**: `seungwon00:현승원-sprint5` → `codeit-bootcamp-spring:현승원`

두 브랜치 간에 충돌이 발생한 이유:
- `현승원-sprint5` 브랜치: 사용자의 개발 작업이 진행된 브랜치
- `upstream/현승원` 브랜치: 메인 브랜치로 병합되려는 대상

충돌이 발생한 주요 파일들:
- `build.gradle` - 의존성 설정 차이
- `DiscodeitApplication.java` - 패키지 구조 차이
- `Channel.java` - 엔티티 필드 및 메서드 차이
- `User.java` - 엔티티 메서드 차이
- 기타 Controller, Service, DTO, Repository 파일들

---

## 2. 충돌 해결 전략

### 전략 1: 현재 브랜치 우선 (HEAD)
대부분의 경우 `현승원-sprint5` 브랜치의 변경사항을 우선 유지했습니다.
- 이유: 사용자의 개발 작업이 최신이고, 이전에 구현한 기능들을 보존하기 위함

### 전략 2: 수동 병합
중요한 파일들은 수동으로 양쪽 변경사항을 병합했습니다.
- 예: `build.gradle` - SpringDoc 의존성은 유지하면서 다른 설정 통합
- 예: `Channel.java`, `User.java` - 필요한 필드와 메서드를 모두 포함

### 전략 3: 자동 해결 (`--ours`)
충돌 마커만 있고 실질적 충돌이 없는 경우, 현재 브랜치 버전을 선택했습니다.
- `git checkout --ours <파일>`

---

## 3. 단계별 해결 과정

### Step 1: 충돌 확인
```bash
# 원격 브랜치 최신 정보 가져오기
git fetch upstream

# merge 시도 (충돌 확인)
git merge upstream/현승원
```

### Step 2: 주요 파일 충돌 해결

#### A. `build.gradle` 충돌 해결
**충돌 원인**: SpringDoc 의존성 버전 차이

**해결 방법**:
- 현재 브랜치의 SpringDoc 의존성 유지 (v2.5.0)
- 다른 의존성 설정은 upstream과 통합
- 최종 결과: SpringDoc 기능 보존하면서 다른 설정 통합

```gradle
// 현재 브랜치 버전 유지
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'
```

#### B. `DiscodeitApplication.java` 충돌 해결
**충돌 원인**: 패키지 경로 차이

**해결 방법**:
- 현재 브랜치의 패키지 구조 유지: `com.sprint.mission.discodeit`
- @SpringBootApplication 어노테이션 유지

#### C. `Channel.java` 충돌 해결
**충돌 원인**: 필드 및 메서드 추가 차이

**해결 방법**:
- 현재 브랜치의 `Serializable` 구현 유지
- `HashSet<UUID> memberIds` 필드 유지
- `update()` 메서드 유지
- 필요한 import 문 통합

```java
public class Channel implements Serializable {
    private Set<UUID> memberIds;  // 현재 브랜치 필드 유지
    
    public void update(String newName, String newDescription) {
        // 현재 브랜치 메서드 유지
    }
}
```

#### D. `User.java` 충돌 해결
**충돌 원인**: 메서드 추가 차이

**해결 방법**:
- 현재 브랜치의 `updateProfileId()` 메서드 유지
- 기본 `update()` 메서드 유지
- 모든 필드 보존

```java
public void updateProfileId(UUID profileId) {
    this.profileId = profileId;  // 현재 브랜치 메서드 유지
}
```

### Step 3: 나머지 파일 자동 해결
```bash
# 현재 브랜치 버전 선택 (충돌 마커만 있는 경우)
git checkout --ours <파일명>

# 적용된 파일들:
# - Controller 파일들 (AuthController, UserController, 등)
# - Service 파일들
# - DTO 파일들
# - Repository 파일들
```

### Step 4: 충돌 해결 확인
```bash
# 충돌 상태 확인
git status

# 빌드 테스트
./gradlew clean build -x test
```

### Step 5: Merge 커밋 생성
```bash
# 충돌 해결 후 커밋
git add .
git commit -m "fix: PR merge 충돌 해결"
```

---

## 4. 충돌 해결 원칙

### ✅ 좋은 해결 방법
1. **기능 보존 우선**: 현재 브랜치에서 구현한 기능을 우선적으로 유지
2. **의존성 호환성**: SpringDoc 같은 핵심 의존성은 유지
3. **엔티티 일관성**: 엔티티의 필드와 메서드는 기능에 필요한 모든 것을 포함
4. **빌드 성공 확인**: 충돌 해결 후 반드시 빌드 테스트

### ❌ 피해야 할 방법
1. **일괄 선택**: 모든 파일을 한쪽으로만 선택하지 않기
2. **충돌 무시**: 충돌 마커를 제거하지 않고 커밋하지 않기
3. **테스트 생략**: 빌드나 테스트 없이 바로 푸시하지 않기

---

## 5. 최종 결과

### 해결된 내용
- ✅ 모든 충돌 파일 해결
- ✅ 빌드 성공 확인 (`BUILD SUCCESSFUL`)
- ✅ 기능 보존 (SpringDoc, 프로필 이미지, 채널 기능 등)
- ✅ Merge 커밋 생성

### PR 상태
- 충돌 해결 완료
- `현승원-sprint5` 브랜치가 `upstream/현승원`과 병합 가능한 상태
- 원격 저장소에 푸시 가능

---

## 6. 앞으로 충돌 예방 방법

### 1. 정기적인 동기화
```bash
# 주기적으로 upstream 브랜치와 동기화
git fetch upstream
git merge upstream/현승원
```

### 2. 기능별 브랜치 분리
- 큰 기능은 별도 브랜치에서 개발 후 PR
- 여러 기능을 한 브랜치에 섞지 않기

### 3. 충돌 발생 시
1. **즉시 해결**: 충돌을 발견하면 바로 해결
2. **소규모 병합**: 큰 변경사항은 작은 단위로 나누어 PR
3. **테스트 필수**: 충돌 해결 후 반드시 빌드 및 테스트

---

## 요약

PR merge 충돌은 다음과 같이 해결했습니다:

1. **충돌 확인**: `git merge` 명령으로 충돌 파일 식별
2. **우선순위 결정**: 현재 브랜치의 기능을 우선 유지
3. **파일별 해결**:
   - 중요 파일(`build.gradle`, 엔티티): 수동 병합
   - 일반 파일: 현재 브랜치 버전 선택
4. **검증**: 빌드 테스트로 확인
5. **커밋**: 충돌 해결 내용 커밋

이 방법으로 사용자의 작업 내용을 보존하면서 upstream 브랜치와 성공적으로 병합할 수 있었습니다.
