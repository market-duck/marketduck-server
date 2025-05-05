# 웹소켓 인증 가이드

## 개요

이 가이드는 MarketDuck 웹소켓 서버에 인증된 연결을 설정하기 위한 방법을 설명합니다. 모든 웹소켓 연결은 JWT 토큰을 사용한 인증이 필요합니다.

## 웹소켓 연결 방법

### 엔드포인트

- 웹소켓 연결 엔드포인트: `/ws-chat`
- SockJS를 통한 연결도 지원합니다.

### 인증 방법

웹소켓 연결 시 다음 두 가지 방법 중 하나로 JWT 토큰을 전달할 수 있습니다:

#### 1. Authorization 헤더 사용

```javascript
// JavaScript/TypeScript 예제
const socket = new WebSocket("ws://your-server-url/ws-chat");
socket.setRequestHeader("Authorization", "Bearer YOUR_JWT_TOKEN");
```

#### 2. URL 파라미터 사용

URL 파라미터를 통해 토큰을 전달할 수도 있습니다:

```javascript
// JavaScript/TypeScript 예제
const socket = new WebSocket(
  "ws://your-server-url/ws-chat?token=YOUR_JWT_TOKEN"
);
```

SockJS 사용 시:

```javascript
// JavaScript/TypeScript 예제 (SockJS와 STOMP 사용)
const socket = new SockJS(
  "http://your-server-url/ws-chat?token=YOUR_JWT_TOKEN"
);
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
  console.log("Connected: " + frame);
  // 채팅방 구독 등의 작업 수행
  stompClient.subscribe("/sub/chat/room/roomId", function (message) {
    // 메시지 처리
  });
});
```

## 메시지 발행 및 구독

### 메시지 구독 (클라이언트 → 서버)

특정 채팅방의 메시지를 구독하려면:

```javascript
stompClient.subscribe("/sub/chat/room/{roomId}", function (message) {
  const receivedMessage = JSON.parse(message.body);
  // 메시지 처리 로직
});
```

### 메시지 발행 (클라이언트 → 서버)

채팅방에 메시지를 보내려면:

```javascript
const chatMessage = {
  roomId: "ROOM_ID",
  sender: "USER_NAME",
  message: "Hello, world!",
  sendTime: new Date().toISOString()
};

stompClient.send("/pub/chat/message", {}, JSON.stringify(chatMessage));
```

## 오류 처리

인증 실패 시 서버는 HTTP 401 (Unauthorized) 상태 코드로 응답하며 연결이 종료됩니다. 클라이언트에서는 이러한 상황을 처리하고 필요한 경우 토큰을 갱신한 후 다시 연결을 시도해야 합니다.

## 보안 고려사항

- 항상 HTTPS/WSS를 사용하여 통신 내용과 토큰을 보호하세요.
- 토큰이 만료되면 새로운 토큰을 얻어 재연결해야 합니다.
- 프로덕션 환경에서는 URL 파라미터보다 헤더를 통한 인증 방식을 권장합니다.
