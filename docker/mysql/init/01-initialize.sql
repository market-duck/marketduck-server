-- 데이터베이스가 없는 경우 생성 (docker-compose에서 이미 처리됨)
CREATE DATABASE IF NOT EXISTS marketduck CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 기본 사용자 권한 부여
GRANT ALL PRIVILEGES ON marketduck.* TO 'marketduck'@'%';
FLUSH PRIVILEGES;

-- 데이터베이스 선택
USE marketduck;

-- 초기 데이터가 필요한 경우 여기에 추가 