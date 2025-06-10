#!/bin/bash

# 설정
DOCKER_REGISTRY=${DOCKER_REGISTRY:-"sussa1933"}
IMAGE_NAME="marketduck"
TAG="dev"

# 스크립트 경로 기준으로 프로젝트 루트 디렉토리 설정
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 빌드 정보 출력
echo "===== DEV 환경 빌드 정보 ====="
echo "이미지 이름: $DOCKER_REGISTRY/$IMAGE_NAME:$TAG"
echo "프로필: dev"
echo "프로젝트 경로: $PROJECT_ROOT"
echo "=========================="

# 환경 변수 로드
if [ -f "$PROJECT_ROOT/.env.dev" ]; then
    export $(cat "$PROJECT_ROOT/.env.dev" | grep -v '^#' | xargs)
    echo "개발 환경 변수 로드 완료"
elif [ -f "$PROJECT_ROOT/.env" ]; then
    export $(cat "$PROJECT_ROOT/.env" | grep -v '^#' | xargs)
    echo "환경 변수 로드 완료"
fi

# Docker Hub 로그인 (필요한 경우)
if [ ! -z "$DOCKER_USERNAME" ] && [ ! -z "$DOCKER_PASSWORD" ]; then
    echo "Docker Hub 로그인 중..."
    echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
    if [ $? -ne 0 ]; then
        echo "Docker Hub 로그인 실패!"
        exit 1
    fi
    echo "Docker Hub 로그인 성공!"
fi

# 프로젝트 루트 디렉토리로 이동
cd "$PROJECT_ROOT"

# Docker 이미지 빌드 (캐시 활용)
echo "Docker 이미지 빌드 시작 (dev 프로필)..."
DOCKER_BUILDKIT=1 docker buildx build --platform linux/amd64 \
    --progress=plain \
    --network=host \
    --build-arg BUILDKIT_INLINE_CACHE=1 \
    --cache-from $DOCKER_REGISTRY/$IMAGE_NAME:dev \
    --load \
    -t $DOCKER_REGISTRY/$IMAGE_NAME:$TAG \
    -t $DOCKER_REGISTRY/$IMAGE_NAME:dev-latest .

# 빌드 성공 여부 확인
if [ $? -eq 0 ]; then
    echo "Docker 이미지 빌드 성공!"
else
    echo "Docker 이미지 빌드 실패!"
    exit 1
fi

# Docker 이미지 푸시
echo "Docker 이미지 푸시 시작..."
docker push $DOCKER_REGISTRY/$IMAGE_NAME:$TAG
docker push $DOCKER_REGISTRY/$IMAGE_NAME:dev-latest

# 푸시 성공 여부 확인
if [ $? -eq 0 ]; then
    echo "Docker 이미지 푸시 성공!"
    echo "이미지: $DOCKER_REGISTRY/$IMAGE_NAME:$TAG"
else
    echo "Docker 이미지 푸시 실패!"
    exit 1
fi

# 로컬 이미지 삭제 (선택사항 - 개발 환경에서는 캐시 활용을 위해 유지할 수도 있음)
read -p "로컬 이미지를 삭제하시겠습니까? (y/N) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "로컬 이미지 삭제 중..."
    docker rmi $DOCKER_REGISTRY/$IMAGE_NAME:$TAG
    docker rmi $DOCKER_REGISTRY/$IMAGE_NAME:dev-latest
    
    if [ $? -eq 0 ]; then
        echo "로컬 이미지 삭제 성공!"
    else
        echo "로컬 이미지 삭제 실패! (이미 삭제되었거나 사용 중일 수 있습니다)"
    fi
else
    echo "로컬 이미지를 유지합니다."
fi

echo "===== 작업 완료 ====="
echo "Docker Hub에 푸시된 이미지: $DOCKER_REGISTRY/$IMAGE_NAME:$TAG"
echo "====================" 