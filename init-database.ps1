# PostgreSQL 데이터베이스 초기화 PowerShell 스크립트
# 실행 방법: PowerShell에서 .\init-database.ps1 실행

Write-Host "PostgreSQL 데이터베이스 초기화를 시작합니다..." -ForegroundColor Green

# PostgreSQL 설치 경로 확인 (기본 경로)
$psqlPath = "C:\Program Files\PostgreSQL\*\bin\psql.exe"
$psql = Get-Item $psqlPath -ErrorAction SilentlyContinue | Select-Object -First 1

if (-not $psql) {
    Write-Host "PostgreSQL을 찾을 수 없습니다. PATH에 psql이 있는지 확인하세요." -ForegroundColor Red
    Write-Host "또는 다음 명령어를 수동으로 실행하세요:" -ForegroundColor Yellow
    Write-Host "  psql -U postgres -f init-database.sql" -ForegroundColor Yellow
    exit 1
}

Write-Host "PostgreSQL 경로: $($psql.FullName)" -ForegroundColor Cyan

# postgres 사용자 비밀번호 입력 요청
$postgresPassword = Read-Host "PostgreSQL postgres 사용자 비밀번호를 입력하세요" -AsSecureString
$BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($postgresPassword)
$plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

# 환경 변수 설정
$env:PGPASSWORD = $plainPassword

try {
    Write-Host "`n1. 데이터베이스 생성 확인 중..." -ForegroundColor Cyan
    $env:PGPASSWORD = $plainPassword
    $dbExists = & $psql.FullName -U postgres -tAc "SELECT 1 FROM pg_database WHERE datname='discodeit'"
    
    if (-not $dbExists) {
        Write-Host "데이터베이스가 존재하지 않습니다. 생성 중..." -ForegroundColor Yellow
        & $psql.FullName -U postgres -c "CREATE DATABASE discodeit OWNER discodeit_user"
        if ($LASTEXITCODE -ne 0) {
            Write-Host "데이터베이스 생성 중 오류가 발생했습니다." -ForegroundColor Red
            exit 1
        }
        Write-Host "데이터베이스가 생성되었습니다." -ForegroundColor Green
    } else {
        Write-Host "데이터베이스가 이미 존재합니다." -ForegroundColor Green
    }
    
    Write-Host "`n2. 사용자 생성 및 권한 부여 중..." -ForegroundColor Cyan
    & $psql.FullName -U postgres -f init-database.sql
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n3. public 스키마 권한 부여 중..." -ForegroundColor Cyan
        $env:PGPASSWORD = $plainPassword
        & $psql.FullName -U postgres -d discodeit -c "GRANT ALL ON SCHEMA public TO discodeit_user;"
        & $psql.FullName -U postgres -d discodeit -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO discodeit_user;"
        
        Write-Host "`n4. 스키마 생성 중..." -ForegroundColor Cyan
        $env:PGPASSWORD = "discodeit1234"
        & $psql.FullName -U discodeit_user -d discodeit -f src\main\resources\schema.sql
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n데이터베이스 초기화가 완료되었습니다!" -ForegroundColor Green
            Write-Host "데이터베이스: discodeit" -ForegroundColor Cyan
            Write-Host "사용자: discodeit_user" -ForegroundColor Cyan
            Write-Host "비밀번호: discodeit1234" -ForegroundColor Cyan
        } else {
            Write-Host "`n스키마 생성 중 오류가 발생했습니다." -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "`n사용자 생성 및 권한 부여 중 오류가 발생했습니다." -ForegroundColor Red
        exit 1
    }
} finally {
    # 환경 변수 정리
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}
