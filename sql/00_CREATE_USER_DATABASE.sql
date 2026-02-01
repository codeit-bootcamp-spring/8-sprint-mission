-- --------------------------------------------------------
-- db 생성 및 유저 권한 할당 (필요하면 실행)
-- --------------------------------------------------------
-- 1. 유저 생성: 이미 생성했다면 SKIP
-- 접속 유저: root 계정인 postgres로 진행
-- 접속 대상: postgres 데이터베이스의 public 스키마
CREATE USER discodeit_user PASSWORD 'discodeit1234' CREATEDB;

-- 2. 데이터베이스 생성
CREATE DATABASE discodeit
    WITH
    OWNER = discodeit_user
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TEMPLATE template0;

-- 3. 스키마 생성 (discodeit_user 계정으로 진행)
-- 접속 유저: 일반 계정인 discodeit_user로 진행
-- 접속 대상: discodeit 데이터베이스의 public 스키마
CREATE SCHEMA IF NOT EXISTS discodeit;
-- DROP SCHEMA discodeit;

-- 4. 권한 할당
GRANT ALL PRIVILEGES ON SCHEMA discodeit TO ohgiraffers;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA discodeit TO discodeit_user;

-- 5. 검색 경로 설정
ALTER ROLE discodeit_user SET search_path TO discodeit;
SHOW search_path;

-- 6. 최종 접속 설정
-- 접속 유저: 일반 계정인 discodeit_user로 진행
-- 접속 대상: discodeit 데이터베이스의 discodeit 스키마