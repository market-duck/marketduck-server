#!/bin/bash

# MarketDuck 개발 환경 중지 스크립트

# 색상 코드
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
BLUE="\033[0;34m"
NC="\033[0m" # No Color

echo -e "${BLUE}=== MarketDuck 개발 환경 종료 ===${NC}"

# 컨테이너 상태 확인
echo -e "${YELLOW}현재 실행 중인 컨테이너 확인...${NC}"
docker-compose ps

# 환경 종료 확인
read -p "개발 환경을 종료하시겠습니까? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Docker Compose 환경 종료 중...${NC}"
    docker-compose down
    echo -e "${GREEN}개발 환경이 성공적으로 종료되었습니다.${NC}"
else
    echo -e "${RED}작업이 취소되었습니다.${NC}"
    exit 0
fi

# 볼륨 삭제 확인
read -p "데이터 볼륨도 삭제하시겠습니까? (주의: 모든 데이터가 삭제됩니다) (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${RED}볼륨 삭제 중...${NC}"
    docker-compose down -v
    echo -e "${GREEN}모든 컨테이너와 볼륨이 삭제되었습니다.${NC}"
fi 