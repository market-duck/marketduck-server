package com.devgang.marketduck.api.chat.controller;

import com.devgang.marketduck.annotation.UserSession;
import com.devgang.marketduck.domain.chat.dto.ChatImageResponseDto;
import com.devgang.marketduck.domain.chat.dto.ChatMessageDto;
import com.devgang.marketduck.domain.chat.dto.ChatRoomDto;
import com.devgang.marketduck.domain.user.entity.User;
import com.devgang.marketduck.dto.ResponseDto;
import com.devgang.marketduck.dto.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Chat API", description = """
        채팅 관련 API

        ### WebSocket 연동 정보
        - 웹소켓 연결 주소: `ws://[도메인]/ws-chat`
        - 메시지 구독: `/sub/chat/room/{sessionId}`
        - 메시지 발행: `/pub/chat/message`

        ### 채팅 기능 플로우
        1. 채팅방 생성: 피드 ID와 수신자 ID로 채팅방 생성 API 호출
        2. 채팅방 정보에서 sessionId 추출하여 웹소켓 연결 및 구독
        3. 메시지 전송: WebSocket 또는 REST API로 가능
        4. 이미지 전송: 먼저 이미지 업로드 API로 이미지 저장 후, 반환된 URL로 이미지 메시지 전송
        """)
public interface ChatControllerIfs {

    class ChatRoomResponse extends ResponseDto<ChatRoomDto> {
    }

    class ChatRoomsResponse extends ResponseDto<List<ChatRoomDto>> {
    }

    class ChatMessageResponse extends ResponseDto<ChatMessageDto> {
    }

    class ChatRoomPageResponse extends PageResponseDto<ChatRoomDto> {
    }

    class ChatImagesResponse extends ResponseDto<List<ChatImageResponseDto>> {
    }

    @Operation(summary = "채팅방 생성", description = """
            피드를 기준으로 새로운 채팅방을 생성합니다.

            - 채팅방은 피드, 발신자, 수신자를 기준으로 생성됩니다.
            - 이미 동일한 피드, 발신자, 수신자로 생성된 채팅방이 있으면 해당 채팅방이 반환됩니다.
            - 비활성화된 채팅방이 있으면 다시 활성화됩니다.
            - 응답에 포함된 sessionId를 사용하여 WebSocket 채팅방을 구독해야 합니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ChatRoomResponse.class))
            })
    })
    ResponseEntity<ResponseDto<ChatRoomDto>> createChatRoom(
            @RequestParam @Parameter(description = "피드 ID", required = true) Long feedId,
            @RequestParam @Parameter(description = "수신자 ID", required = true) Long receiverId,
            @UserSession @Parameter(hidden = true) User user);

    @Operation(summary = "내 채팅방 목록 조회", description = """
            로그인한 사용자의 모든 활성화된 채팅방을 조회합니다.

            - 판매자 또는 구매자로 참여한 모든 활성 채팅방이 조회됩니다.
            - 각 채팅방의 최근 메시지 1개와 읽지 않은 메시지 수가 함께 제공됩니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ChatRoomsResponse.class))
            })
    })
    ResponseEntity<ResponseDto<List<ChatRoomDto>>> getMyChatRooms(
            @UserSession @Parameter(hidden = true) User user);

    @Operation(summary = "채팅방 상세 조회", description = """
            특정 채팅방의 정보와 최근 메시지 30개를 조회합니다.

            - 채팅방에 참여한 사용자만 조회할 수 있습니다.
            - 최근 30개 메시지가 포함됩니다.
            - 메시지 조회 시 읽지 않은 메시지는 자동으로 읽음 처리됩니다.
            - 채팅방 조회 시 Redis 토픽이 생성됩니다(없는 경우).
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ChatRoomPageResponse.class))
            })
    })
    ResponseEntity<PageResponseDto<ChatRoomDto>> getChatRoom(
            @PathVariable @Parameter(description = "채팅방 ID", required = true) Long chatRoomId,
            @UserSession @Parameter(hidden = true) User user);

    @Operation(summary = "채팅방 나가기", description = """
            채팅방을 비활성화하여 사용자의 채팅방 목록에서 제외합니다.

            - 채팅방이 비활성화되며 사용자의 채팅방 목록에서 더 이상 표시되지 않습니다.
            - 모든 참여자가 채팅방을 나가면 채팅방은 자동으로 비활성화됩니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json")
            })
    })
    ResponseEntity<ResponseDto<Void>> leaveChatRoom(
            @PathVariable @Parameter(description = "채팅방 ID", required = true) Long chatRoomId,
            @UserSession @Parameter(hidden = true) User user);

    @Operation(summary = "관리자용 모든 채팅방 조회", description = """
            관리자만 사용 가능한 모든 채팅방 조회 API입니다.

            - 활성화/비활성화 상태 관계없이 모든 채팅방이 조회됩니다.
            - 관리자 권한이 있는 사용자만 접근 가능합니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ChatRoomsResponse.class))
            })
    })
    ResponseEntity<ResponseDto<List<ChatRoomDto>>> getAllChatRoomsForAdmin();

    @Operation(summary = "텍스트 메시지 전송", description = """
            일반 텍스트 메시지를 특정 채팅방에 전송합니다.

            - WebSocket 외에도 이 REST API를 통해 메시지 전송이 가능합니다.
            - 메시지는 데이터베이스에 저장되고 채팅방 구독자에게 실시간으로 전달됩니다.

            WebSocket으로 메시지 전송 시:
            ```javascript
            const message = {
              chatRoomId: 1,
              senderId: 2,
              content: '안녕하세요',
              sessionId: 'uuid-session-id',
              messageType: 'TEXT'
            };
            stompClient.send('/pub/chat/message', {}, JSON.stringify(message));
            ```
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ChatMessageResponse.class))
            })
    })
    ResponseEntity<ResponseDto<ChatMessageDto>> sendTextMessage(
            @PathVariable @Parameter(description = "채팅방 ID", required = true) Long chatRoomId,
            @RequestParam @Parameter(description = "메시지 내용", required = true) String content,
            @UserSession @Parameter(hidden = true) User user);

    @Operation(summary = "이미지 메시지 전송", description = """
            이미지 URL을 메시지로 전송합니다.

            - 이미지 업로드 API에서 반환된 URL을 이용하여 이미지 메시지를 전송합니다.
            - 텍스트 메시지와 동일하게 WebSocket 또는 REST API로 전송 가능합니다.

            WebSocket으로 이미지 메시지 전송 시:
            ```javascript
            const message = {
              chatRoomId: 1,
              senderId: 2,
              content: 'https://example.com/images/123.jpg',
              sessionId: 'uuid-session-id',
              messageType: 'IMAGE'
            };
            stompClient.send('/pub/chat/message', {}, JSON.stringify(message));
            ```
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ChatMessageResponse.class))
            })
    })
    ResponseEntity<ResponseDto<ChatMessageDto>> sendImageMessage(
            @PathVariable @Parameter(description = "채팅방 ID", required = true) Long chatRoomId,
            @RequestParam @Parameter(description = "이미지 URL", required = true) String imageUrl,
            @UserSession @Parameter(hidden = true) User user);

    @Operation(summary = "채팅 이미지 업로드", description = """
            채팅에 첨부할 이미지를 업로드합니다.

            - 최대 5개의 이미지를 한 번에 업로드할 수 있습니다.
            - 이미지 파일만 업로드 가능합니다.
            - 업로드된 이미지의 URL을 반환합니다.
            - 반환된 URL을 이미지 메시지 전송 API에 사용하여 이미지 메시지를 전송합니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상 응답", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ChatImagesResponse.class))
            })
    })
    ResponseEntity<ResponseDto<List<ChatImageResponseDto>>> postChatImage(
            @RequestPart("file") @Parameter(description = "이미지 파일(들)", required = true) MultipartFile[] multipartFile,
            @UserSession @Parameter(hidden = true) User user);
}