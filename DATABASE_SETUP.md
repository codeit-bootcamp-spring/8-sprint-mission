# PostgreSQL 데이터베이스 설정 가이드

## 데이터베이스 정보
- **데이터베이스명**: `discodeit`
- **사용자명**: `discodeit_user`
- **비밀번호**: `discodeit1234`

## 초기화 방법

### 방법 1: PowerShell 스크립트 사용 (권장)

Windows PowerShell에서 다음 명령어를 실행하세요:

```powershell
.\init-database.ps1
```

스크립트가 다음 작업을 자동으로 수행합니다:
1. 데이터베이스 생성 (없는 경우)
2. 사용자 생성 및 권한 부여
3. 스키마 생성

### 방법 2: 수동 실행

#### 1단계: 데이터베이스 및 사용자 생성

PostgreSQL에 `postgres` 사용자로 접속:

```bash
psql -U postgres
```

다음 SQL 명령어들을 실행:

```sql
-- 사용자 생성
CREATE USER discodeit_user WITH PASSWORD 'discodeit1234';

-- 데이터베이스 생성
CREATE DATABASE discodeit OWNER discodeit_user;

-- 권한 부여
GRANT ALL PRIVILEGES ON DATABASE discodeit TO discodeit_user;

-- 종료
\q
```

#### 2단계: 스키마 생성

```bash
psql -U discodeit_user -d discodeit -f src/main/resources/schema.sql
```

또는 `init-database.sql`을 실행:

```bash
psql -U postgres -f init-database.sql
```

그 다음 스키마 생성:

```bash
psql -U discodeit_user -d discodeit -f src/main/resources/schema.sql
```

## 확인

데이터베이스가 제대로 생성되었는지 확인:

```bash
psql -U discodeit_user -d discodeit -c "\dt"
```

테이블 목록이 표시되면 성공입니다.

## 문제 해결

### "데이터베이스가 이미 존재합니다" 오류
- 데이터베이스가 이미 존재하는 경우, 기존 데이터베이스를 삭제하거나 다른 이름을 사용하세요.
- 삭제: `DROP DATABASE discodeit;` (주의: 모든 데이터가 삭제됩니다)

### "사용자가 이미 존재합니다" 오류
- 사용자가 이미 존재하는 경우, 기존 사용자를 삭제하거나 다른 이름을 사용하세요.
- 삭제: `DROP USER discodeit_user;` (주의: 먼저 데이터베이스 소유권을 변경해야 할 수 있습니다)

### 연결 오류
- PostgreSQL 서비스가 실행 중인지 확인하세요.
- Windows: 서비스 관리자에서 "postgresql-x64-XX" 서비스 확인
- 포트 5432가 열려있는지 확인하세요.
