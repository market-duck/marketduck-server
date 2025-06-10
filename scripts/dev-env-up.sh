#!/bin/bash

# MarketDuck 개발 환경 실행 스크립트

# 색상 코드
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
BLUE="\033[0;34m"
NC="\033[0m" # No Color

echo -e "${BLUE}=== MarketDuck 개발 환경 시작 ===${NC}"

# 로그 디렉토리 생성
echo -e "${YELLOW}로그 디렉토리 생성...${NC}"
mkdir -p logs

# Docker Compose 환경 실행
echo -e "${YELLOW}Docker Compose 환경 시작...${NC}"
docker-compose up -d

# 컨테이너 상태 확인
echo -e "${YELLOW}컨테이너 상태 확인...${NC}"
sleep 3
docker-compose ps

# 애플리케이션 로그 확인 (선택 사항)
read -p "애플리케이션 로그를 확인하시겠습니까? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}애플리케이션 로그 확인 중...${NC}"
    docker-compose logs -f marketduck
fi

echo -e "${GREEN}개발 환경이 성공적으로 시작되었습니다!${NC}"
echo -e "${YELLOW}중지하려면: ${NC}docker-compose down"
echo -e "${YELLOW}로그 확인: ${NC}docker-compose logs -f [servicename]"
echo -e "${YELLOW}서비스 목록: ${NC}marketduck, mysql, redis" 