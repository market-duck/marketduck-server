#!/bin/sh

# 프로덕션 서버 배포 스크립트
# 주의: 이 스크립트는 프로덕션 서버에서 실행해야 합니다.

# 색상 코드
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
BLUE="\033[0;34m"
NC="\033[0m" # No Color

# 기본 설정 값 (파일이 없어도 이 값으로 진행)
APP_NAME="marketduck"
APP_VERSION="latest"
DOCKER_REGISTRY="sussa1933"
DB_USER="marketduck"
DB_PASSWORD="sussa1933!"
DB_ROOT_PASSWORD="sussa1933!"
REDIS_PASSWORD="sussa1933!"
DATA_PATH="/var/marketduck"

# 설정 파일 로드 (있는 경우에만)
CONFIG_FILE=".env.prod"
if [ -f "$CONFIG_FILE" ]; then
    echo -e "${GREEN}설정 파일을 로드합니다: $CONFIG_FILE${NC}"
    # . 명령 실패해도 스크립트 계속 진행
    . "$CONFIG_FILE" || echo -e "${YELLOW}설정 파일 로드 중 경고가 있었습니다. 일부 기본값을 사용합니다.${NC}"
else
    echo -e "${YELLOW}설정 파일($CONFIG_FILE)이 없습니다. 기본값을 사용합니다.${NC}"
fi

# 배포 시작
echo -e "${BLUE}===== MarketDuck 프로덕션 배포 시작 =====${NC}"
echo -e "애플리케이션: $DOCKER_REGISTRY/$APP_NAME:$APP_VERSION"
echo -e "데이터 경로: $DATA_PATH"

# 필요한 디렉토리 생성
echo -e "${YELLOW}필요한 디렉토리 생성 중...${NC}"
mkdir -p $DATA_PATH/mysql $DATA_PATH/redis logs backup config docker/mysql/{init,conf}

# 현재 컨테이너 상태 확인
echo -e "${YELLOW}현재 실행 중인 컨테이너 확인...${NC}"
docker ps -a | grep $APP_NAME

# 최신 Docker 이미지 가져오기
echo -e "${YELLOW}최신 Docker 이미지 가져오기...${NC}"
docker pull $DOCKER_REGISTRY/$APP_NAME:$APP_VERSION

# 배포 확인
echo -e "${YELLOW}프로덕션 환경에 배포를 진행하시겠습니까? (y/N)${NC}"
read REPLY
echo

if [ "$REPLY" = "y" ] || [ "$REPLY" = "Y" ]; then
    echo -e "${GREEN}배포를 진행합니다.${NC}"
else
    echo -e "${RED}배포가 취소되었습니다.${NC}"
    exit 0
fi

# 현재 실행 중인 서비스 백업 (있는 경우)
if [ -d "backup" ]; then
    echo -e "${YELLOW}기존 데이터 백업 중...${NC}"
    BACKUP_DATE=$(date +%Y%m%d-%H%M%S)
    if [ "$(docker ps -q -f name=$APP_NAME-mysql-prod)" ]; then
        echo -e "${YELLOW}MySQL 데이터 백업 중...${NC}"
        docker exec $APP_NAME-mysql-prod mysqldump -u root -p$DB_ROOT_PASSWORD --all-databases > backup/mysql-$BACKUP_DATE.sql
    fi
    
    # 로그 백업
    if [ -d "logs" ]; then
        echo -e "${YELLOW}로그 파일 백업 중...${NC}"
        tar -czf backup/logs-$BACKUP_DATE.tar.gz logs/
    fi
fi

# 환경 변수 파일 생성
echo -e "${YELLOW}환경 변수 파일 생성 중...${NC}"
cat > .env.prod << EOF
# MarketDuck 프로덕션 환경 설정
APP_NAME=$APP_NAME
APP_VERSION=$APP_VERSION
DOCKER_REGISTRY=$DOCKER_REGISTRY
DB_USER=$DB_USER
DB_PASSWORD=$DB_PASSWORD
DB_ROOT_PASSWORD=$DB_ROOT_PASSWORD
REDIS_PASSWORD=$REDIS_PASSWORD
DATA_PATH=$DATA_PATH
EOF

# 현재 실행 중인 컨테이너 중지 및 제거
echo -e "${YELLOW}기존 컨테이너 중지 및 제거 중...${NC}"
docker compose -f docker-compose.prod.yml down

# 프로덕션 환경 시작
echo -e "${YELLOW}프로덕션 환경 시작 중...${NC}"
docker compose -f docker-compose.prod.yml up -d

# 시작 상태 확인
echo -e "${YELLOW}컨테이너 상태 확인 중...${NC}"
sleep 5
docker compose -f docker-compose.prod.yml ps

# 서비스 상태 확인
echo -e "${YELLOW}서비스 헬스체크 중...${NC}"
sleep 10

# MarketDuck 서비스 헬스체크
if [ "$(docker ps -q -f name=$APP_NAME-prod)" ]; then
    echo -e "${GREEN}$APP_NAME 서비스가 실행 중입니다.${NC}"
    
    # Actuator 헬스체크 (Spring Boot)
    if curl -s http://localhost:8987/actuator/health | grep -q "UP"; then
        echo -e "${GREEN}$APP_NAME 애플리케이션이 정상적으로 실행 중입니다.${NC}"
    else
        echo -e "${RED}$APP_NAME 애플리케이션이 비정상적으로 실행 중입니다. 로그를 확인하세요.${NC}"
        docker compose -f docker-compose.prod.yml logs marketduck
    fi
else
    echo -e "${RED}$APP_NAME 서비스가 실행되지 않았습니다. 로그를 확인하세요.${NC}"
    docker compose -f docker-compose.prod.yml logs marketduck
fi

# 배포 완료
echo -e "${BLUE}===== MarketDuck 프로덕션 배포 완료 =====${NC}"
echo -e "${YELLOW}로그 확인: ${NC}docker compose -f docker-compose.prod.yml logs -f [servicename]"
echo -e "${YELLOW}서비스 목록: ${NC}marketduck, mysql, redis, backup"
echo -e "${YELLOW}환경 중지: ${NC}docker compose -f docker-compose.prod.yml down"
echo -e "${YELLOW}환경 재시작: ${NC}docker compose -f docker-compose.prod.yml restart" 