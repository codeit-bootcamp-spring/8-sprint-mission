-- PostgreSQL 데이터베이스 및 사용자 초기화 스크립트
-- 이 스크립트는 postgres 사용자로 실행해야 합니다.
-- 
-- 실행 방법:
-- 1. psql -U postgres로 접속
-- 2. CREATE DATABASE discodeit OWNER discodeit_user; 실행 (아직 데이터베이스가 없다면)
-- 3. \q로 종료
-- 4. psql -U postgres -f init-database.sql 실행
-- 5. psql -U discodeit_user -d discodeit -f src/main/resources/schema.sql 실행

-- 1. 사용자 생성 (이미 존재하면 오류 발생, 무시 가능)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'discodeit_user') THEN
        CREATE USER discodeit_user WITH PASSWORD 'discodeit1234';
        RAISE NOTICE '사용자 discodeit_user가 생성되었습니다.';
    ELSE
        RAISE NOTICE '사용자 discodeit_user가 이미 존재합니다.';
    END IF;
END
$$;

-- 2. 데이터베이스가 존재하는지 확인하고 권한 부여
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_database WHERE datname = 'discodeit') THEN
        GRANT ALL PRIVILEGES ON DATABASE discodeit TO discodeit_user;
        RAISE NOTICE '데이터베이스 discodeit에 대한 권한이 부여되었습니다.';
    ELSE
        RAISE WARNING '데이터베이스 discodeit이 존재하지 않습니다. 먼저 다음 명령어를 실행하세요:';
        RAISE WARNING 'CREATE DATABASE discodeit OWNER discodeit_user;';
    END IF;
END
$$;

-- 3. public 스키마에 대한 권한 부여 (ddl-auto: update 사용 시 필요)
-- discodeit 데이터베이스에 연결한 후 실행해야 합니다.
-- \c discodeit
-- GRANT ALL ON SCHEMA public TO discodeit_user;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO discodeit_user;
