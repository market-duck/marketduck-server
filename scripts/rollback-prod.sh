#!/bin/sh

# 프로덕션 서버 롤백 스크립트
# 주의: 이 스크립트는 프로덕션 서버에서 실행해야 합니다.

# 색상 코드
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
BLUE="\033[0;34m"
NC="\033[0m" # No Color

# 기본 설정 값 (파일이 없어도 이 값으로 진행)
APP_NAME="marketduck"
DOCKER_REGISTRY="sussa1933"
DB_ROOT_PASSWORD="sussa1933!"

# 설정 파일 로드 (있는 경우에만)
CONFIG_FILE=".env.prod"
if [ -f "$CONFIG_FILE" ]; then
    echo -e "${GREEN}설정 파일을 로드합니다: $CONFIG_FILE${NC}"
    # . 명령 실패해도 스크립트 계속 진행
    . "$CONFIG_FILE" || echo -e "${YELLOW}설정 파일 로드 중 경고가 있었습니다. 일부 기본값을 사용합니다.${NC}"
else
    echo -e "${YELLOW}설정 파일($CONFIG_FILE)이 없습니다. 기본값을 사용합니다.${NC}"
fi

# 사용 가능한 이미지 목록 확인
echo -e "${BLUE}===== MarketDuck 프로덕션 롤백 =====${NC}"
echo -e "${YELLOW}사용 가능한 이미지 목록 확인 중...${NC}"
docker images "$DOCKER_REGISTRY/$APP_NAME" --format "{{.Tag}}"

# 롤백할 버전 선택
read -p "롤백할 버전을 입력하세요 (기본값: 이전 버전): " ROLLBACK_VERSION
ROLLBACK_VERSION=${ROLLBACK_VERSION:-"$(docker images "$DOCKER_REGISTRY/$APP_NAME" --format "{{.Tag}}" | grep -v latest | head -n 1)"}

if [ -z "$ROLLBACK_VERSION" ]; then
    echo -e "${RED}롤백할 버전이 없습니다. 먼저 이미지를 가져오세요.${NC}"
    exit 1
fi

echo -e "${YELLOW}선택한 롤백 버전: $ROLLBACK_VERSION${NC}"

# 롤백 확인
echo -e "${YELLOW}프로덕션 환경을 버전 $ROLLBACK_VERSION으로 롤백하시겠습니까? (y/N)${NC}"
read REPLY
echo

if [ "$REPLY" = "y" ] || [ "$REPLY" = "Y" ]; then
    echo -e "${GREEN}롤백을 진행합니다.${NC}"
else
    echo -e "${RED}롤백이 취소되었습니다.${NC}"
    exit 0
fi

# 현재 실행 중인 서비스 백업
echo -e "${YELLOW}현재 상태 백업 중...${NC}"
BACKUP_DATE=$(date +%Y%m%d-%H%M%S)
mkdir -p backup

# MySQL 백업
if [ "$(docker ps -q -f name=$APP_NAME-mysql-prod)" ]; then
    echo -e "${YELLOW}MySQL 데이터 백업 중...${NC}"
    docker exec $APP_NAME-mysql-prod mysqldump -u root -p$DB_ROOT_PASSWORD --all-databases > backup/mysql-before-rollback-$BACKUP_DATE.sql
fi

# 로그 백업
if [ -d "logs" ]; then
    echo -e "${YELLOW}로그 파일 백업 중...${NC}"
    tar -czf backup/logs-before-rollback-$BACKUP_DATE.tar.gz logs/
fi

# 현재 버전 정보 저장
CURRENT_VERSION=$(docker inspect $APP_NAME-prod --format '{{index .Config.Image}}' | cut -d ':' -f 2)
echo -e "${YELLOW}현재 버전: $CURRENT_VERSION${NC}"
echo "$CURRENT_VERSION" > backup/current-version-$BACKUP_DATE.txt

# 환경 변수 업데이트
echo -e "${YELLOW}환경 변수 업데이트 중...${NC}"
sed -i.bak "s/APP_VERSION=.*/APP_VERSION=$ROLLBACK_VERSION/" .env.prod

# 이미지 가져오기 (없는 경우)
if ! docker images | grep -q "$DOCKER_REGISTRY/$APP_NAME" | grep -q "$ROLLBACK_VERSION"; then
    echo -e "${YELLOW}이미지 가져오기: $DOCKER_REGISTRY/$APP_NAME:$ROLLBACK_VERSION${NC}"
    docker pull $DOCKER_REGISTRY/$APP_NAME:$ROLLBACK_VERSION
fi

# 현재 실행 중인 컨테이너 중지 및 제거
echo -e "${YELLOW}기존 컨테이너 중지 및 제거 중...${NC}"
docker compose -f docker-compose.prod.yml down

# 롤백 버전으로 시작
echo -e "${YELLOW}$ROLLBACK_VERSION 버전으로 서비스 시작 중...${NC}"
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
        echo -e "${GREEN}$APP_NAME 애플리케이션이 정상적으로 롤백되었습니다.${NC}"
    else
        echo -e "${RED}$APP_NAME 애플리케이션이 비정상적으로 실행 중입니다. 로그를 확인하세요.${NC}"
        docker compose -f docker-compose.prod.yml logs marketduck
        
        # 이전 버전으로 복구 제안
        echo -e "${YELLOW}이전 버전($CURRENT_VERSION)으로 복구하시겠습니까? (y/N)${NC}"
        read REPLY
        echo
        if [ "$REPLY" = "y" ] || [ "$REPLY" = "Y" ]; then
            echo -e "${YELLOW}이전 버전($CURRENT_VERSION)으로 복구 중...${NC}"
            sed -i.bak "s/APP_VERSION=.*/APP_VERSION=$CURRENT_VERSION/" .env.prod
            docker compose -f docker-compose.prod.yml down
            docker compose -f docker-compose.prod.yml up -d
        fi
    fi
else
    echo -e "${RED}$APP_NAME 서비스가 실행되지 않았습니다. 로그를 확인하세요.${NC}"
    docker compose -f docker-compose.prod.yml logs marketduck
    
    # 이전 버전으로 복구 제안
    echo -e "${YELLOW}이전 버전($CURRENT_VERSION)으로 복구하시겠습니까? (y/N)${NC}"
    read REPLY
    echo
    if [ "$REPLY" = "y" ] || [ "$REPLY" = "Y" ]; then
        echo -e "${YELLOW}이전 버전($CURRENT_VERSION)으로 복구 중...${NC}"
        sed -i.bak "s/APP_VERSION=.*/APP_VERSION=$CURRENT_VERSION/" .env.prod
        docker compose -f docker-compose.prod.yml down
        docker compose -f docker-compose.prod.yml up -d
    fi
fi

# 롤백 완료
echo -e "${BLUE}===== MarketDuck 프로덕션 롤백 완료 =====${NC}"
echo -e "${YELLOW}롤백 버전: $ROLLBACK_VERSION${NC}"
echo -e "${YELLOW}로그 확인: ${NC}docker compose -f docker-compose.prod.yml logs -f [servicename]"
echo -e "${YELLOW}서비스 목록: ${NC}marketduck, mysql, redis, backup" 