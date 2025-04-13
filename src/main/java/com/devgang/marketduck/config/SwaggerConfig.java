package com.devgang.marketduck.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI openAPI() {
                Server server = new Server();
                server.setDescription("dev");
                server.setUrl("https://marketduck.server-su.site");
                Server local = new Server();
                local.setDescription("local");
                local.setUrl("http://192.168.75.163:8987");

                Server product = new Server();
                product.setDescription("product");
                product.setUrl("https://api.marketduck.site");

                Info info = new Info()
                                .version("v0.0.1")
                                .title("Market Duck API 명세서")
                                .description("""
                                                 Market Duck 백엔드 서버 API 명세서\s
                                                 /api/** -> Access Token Header Required\s
                                                 /open-api/** -> No Access Token Required\s

                                                 ## 피드 기반 채팅 통합 가이드

                                                 ### WebSocket 연결
                                                 - 엔드포인트: `ws://[도메인]/ws-chat`
                                                 - 구독: `/sub/chat/room/{sessionId}`
                                                 - 메시지 발행: `/pub/chat/message`

                                                 ### REST API 엔드포인트

                                                 #### 채팅방 생성
                                                 - URL: `/api/chat/rooms`
                                                 - Method: `POST`
                                                 - Parameters: `feedId`, `receiverId`

                                                 #### 채팅방 목록 조회
                                                 - URL: `/api/chat/rooms`
                                                 - Method: `GET`

                                                 #### 채팅방 상세 조회
                                                 - URL: `/api/chat/rooms/{chatRoomId}`
                                                 - Method: `GET`

                                                 #### 채팅방 나가기
                                                 - URL: `/api/chat/rooms/{chatRoomId}/leave`
                                                 - Method: `PUT`

                                                 #### 텍스트 메시지 전송
                                                 - URL: `/api/chat/rooms/{chatRoomId}/messages`
                                                 - Method: `POST`
                                                 - Parameters: `content`

                                                 #### 이미지 업로드
                                                 - URL: `/api/chat/image`
                                                 - Method: `POST`
                                                 - Content-Type: `multipart/form-data`
                                                 - Parameters: `file` (최대 5개)

                                                 #### 이미지 메시지 전송
                                                 - URL: `/api/chat/rooms/{chatRoomId}/images`
                                                 - Method: `POST`
                                                 - Parameters: `imageUrl`

                                                 ### 채팅 기능 플로우
                                                 1. 피드 상세 페이지에서 채팅하기 버튼 클릭
                                                 2. 채팅방 생성 API 호출 (`POST /api/chat/rooms`)
                                                 3. 반환된 채팅방 정보 중 `sessionId` 저장
                                                 4. WebSocket 연결: `/ws-chat`
                                                 5. 채팅방 구독: `/sub/chat/room/{sessionId}`
                                                 6. 이후 메시지 교환 시작

                                                 ### 메시지 전송 플로우
                                                 1. **텍스트 메시지**
                                                    - WebSocket: `/pub/chat/message`로 메시지 발행
                                                    - REST API: `/api/chat/rooms/{chatRoomId}/messages` 호출

                                                 2. **이미지 메시지**
                                                    - 이미지 업로드: `/api/chat/image` 호출
                                                    - 이미지 URL을 사용하여 메시지 전송:
                                                      - WebSocket: `/pub/chat/message` (messageType: IMAGE)
                                                      - REST API: `/api/chat/rooms/{chatRoomId}/images` 호출

                                                 ### 기존 채팅방 접속 플로우
                                                 1. 채팅방 목록 조회: `/api/chat/rooms` 호출
                                                 2. 채팅방 선택 시 상세 정보 조회: `/api/chat/rooms/{chatRoomId}` 호출
                                                 3. 응답된 최근 메시지 30개 표시
                                                 4. WebSocket 연결 및 채팅방 구독
                                                 5. 메시지 교환 시작


                                                 ```
                                                \s""");

                // SecuritySecheme명
                String jwtSchemeName = "JWT Access Token";
                // API 요청헤더에 인증정보 포함
                SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
                // SecuritySchemes 등록
                Components components = new Components()
                                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                                                .name(jwtSchemeName)
                                                .type(SecurityScheme.Type.HTTP) // HTTP 방식
                                                .scheme("Bearer")
                                                .bearerFormat("JWT")); // 토큰 형식을 지정하는 임의의 문자(Optional)

                OpenAPI result = new OpenAPI()
                                .info(info)
                                .addSecurityItem(securityRequirement)
                                .components(components);
                result.setServers(List.of(server, product, local));

                return result;
        }

}
