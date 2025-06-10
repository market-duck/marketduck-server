# MarketDuck 배포 스크립트

이 디렉토리에는 MarketDuck 애플리케이션의 배포 및 관리를 위한 스크립트가 포함되어 있습니다.

## 배포 스크립트 목록

### 1. 개발 환경 스크립트

- **docker-build-push-dev.sh**: 개발 환경용 Docker 이미지를 빌드하고 Docker Hub에 푸시합니다.
- **docker-build-push.sh**: 프로덕션 환경용 Docker 이미지를 빌드하고 Docker Hub에 푸시합니다.
- **dev-env-up.sh**: 로컬 개발 환경을 Docker Compose로 시작합니다.
- **dev-env-down.sh**: 로컬 개발 환경을 중지합니다.

### 2. 프로덕션 환경 스크립트

- **deploy-prod.sh**: 프로덕션 서버에 애플리케이션을 배포합니다.
- **rollback-prod.sh**: 문제 발생 시 이전 버전으로 롤백합니다.
- **deploy-dev-prod.sh**: 개발 버전을 프로덕션 서버에 배포합니다. (테스트 및 사전 검증용)

## 스크립트 사용법

### 개발 환경

1. **개발 환경 이미지 빌드 및 푸시**:

   ```bash
   ./scripts/docker-build-push-dev.sh
   ```

2. **개발 환경 실행**:

   ```bash
   ./scripts/dev-env-up.sh
   ```

3. **개발 환경 중지**:
   ```bash
   ./scripts/dev-env-down.sh
   ```

### 프로덕션 환경

1. **프로덕션 이미지 빌드 및 푸시**:

   ```bash
   ./scripts/docker-build-push.sh
   ```

2. **프로덕션 서버에 배포**:

   ```bash
   # 서버에서 실행
   ./scripts/deploy-prod.sh
   ```

3. **롤백 실행 (필요시)**:

   ```bash
   # 서버에서 실행
   ./scripts/rollback-prod.sh
   ```

4. **개발 버전을 프로덕션 서버에 배포** (테스트 목적):
   ```bash
   # 서버에서 실행
   ./scripts/deploy-dev-prod.sh
   ```

## 프로덕션 배포 프로세스

1. 로컬에서 이미지 빌드 및 푸시:

   ```bash
   ./scripts/docker-build-push.sh
   ```

2. 서버에 코드 복사 (Git 또는 직접 전송):

   ```bash
   # Git을 사용하는 경우
   git pull origin main

   # 직접 파일 전송의 경우
   scp docker-compose.prod.yml scripts/deploy-prod.sh scripts/rollback-prod.sh user@server:/path/to/app/
   ```

3. 서버에서 배포 스크립트 실행:
   ```bash
   ./scripts/deploy-prod.sh
   ```

## 개발 버전 프로덕션 서버 배포 프로세스

1. 로컬에서 개발 이미지 빌드 및 푸시:

   ```bash
   ./scripts/docker-build-push-dev.sh
   ```

2. 서버에 코드 복사:

   ```bash
   # Git을 사용하는 경우
   git pull origin develop

   # 직접 파일 전송의 경우
   scp docker-compose.dev-prod.yml scripts/deploy-dev-prod.sh user@server:/path/to/app/
   ```

3. 서버에서 개발 버전 배포 스크립트 실행:

   ```bash
   ./scripts/deploy-dev-prod.sh
   ```

4. 개발 버전 테스트 접속:
   ```
   http://서버IP:8988
   ```

## 환경 설정

- **.env.prod**: 프로덕션 환경 변수 파일 (배포 스크립트에서 자동 생성)
- **.env.dev-prod**: 개발 버전 배포 환경 변수 파일 (배포 스크립트에서 자동 생성)
- **docker-compose.prod.yml**: 프로덕션용 Docker Compose 설정 파일
- **docker-compose.dev-prod.yml**: 개발 버전 배포용 Docker Compose 설정 파일

## 데이터 관리

- 프로덕션 데이터는 `${DATA_PATH}` 경로에 저장됩니다 (기본값: `/var/marketduck`)
- 개발 버전 데이터는 `${DATA_PATH}/dev` 경로에 저장됩니다
- 자동 백업은 각각 `backup` 및 `backup-dev` 디렉토리에 저장됩니다
- 로그는 각각 `logs` 및 `logs-dev` 디렉토리에 저장됩니다

## 주의사항

- 프로덕션 배포 스크립트는 실제 프로덕션 서버에서만 실행하세요.
- 중요한 배포 전에는 데이터를 항상 백업하세요.
- 민감한 정보(비밀번호, API 키 등)는 `.env.prod` 및 `.env.dev-prod` 파일에 보관하고 버전 관리에서 제외하세요.
- 개발 버전과 프로덕션 버전은 서로 다른 포트와 데이터 경로를 사용하여 공존할 수 있습니다.
- 프로덕션 서버의 리소스가 충분한지 확인 후 개발 버전을 배포하세요.
