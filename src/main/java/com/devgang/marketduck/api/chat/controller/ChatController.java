package com.devgang.marketduck.api.chat.controller;

import com.devgang.marketduck.annotation.UserSession;
import com.devgang.marketduck.constant.MessageType;
import com.devgang.marketduck.domain.chat.dto.ChatImageResponseDto;
import com.devgang.marketduck.domain.chat.dto.ChatMessageDto;
import com.devgang.marketduck.domain.chat.dto.ChatRoomDto;
import com.devgang.marketduck.domain.chat.service.ChatService;
import com.devgang.marketduck.domain.user.entity.User;
import com.devgang.marketduck.dto.ResponseDto;
import com.devgang.marketduck.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.devgang.marketduck.dto.PageResponseDto;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController implements ChatControllerIfs {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * /pub/chat/message로 전송되는 메시지를 처리
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessageDto message) {
        // 채팅 메시지 처리 및 저장
        ChatMessageDto savedMessage = chatService.sendMessage(
                message.getChatRoomId(),
                message.getSenderId(),
                message.getContent(),
                message.getMessageType());

        // WebSocket으로 전송
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getSessionId(), savedMessage);
    }

    /**
     * 채팅방 생성
     */
    @Override
    @PostMapping("/rooms")
    public ResponseEntity<ResponseDto<ChatRoomDto>> createChatRoom(
            @RequestParam Long feedId,
            @RequestParam Long receiverId,
            @UserSession User user) {

        Long senderId = user.getUserId();
        ChatRoomDto chatRoom = chatService.createChatRoom(feedId, senderId, receiverId);

        return ResponseEntity.ok(ResponseDto.of(chatRoom, Result.ok()));
    }

    /**
     * 사용자의 모든 채팅방 목록 조회
     */
    @Override
    @GetMapping("/rooms")
    public ResponseEntity<ResponseDto<List<ChatRoomDto>>> getMyChatRooms(
            @UserSession User user) {

        Long userId = user.getUserId();
        List<ChatRoomDto> chatRooms = chatService.getChatRooms(userId);

        return ResponseEntity.ok(ResponseDto.of(chatRooms, Result.ok()));
    }

    /**
     * 특정 채팅방 상세 조회
     */
    @Override
    @GetMapping("/rooms/{chatRoomId}")
    public ResponseEntity<PageResponseDto<ChatRoomDto>> getChatRoom(
            @PathVariable Long chatRoomId,
            @UserSession User user) {

        Long userId = user.getUserId();
        PageResponseDto<ChatRoomDto> chatRoom = chatService.getChatRoomWithPageMessages(chatRoomId, userId);

        return ResponseEntity.ok(chatRoom);
    }

    /**
     * 채팅방 나가기 (비활성화)
     */
    @Override
    @PatchMapping("/rooms/{chatRoomId}/leave")
    public ResponseEntity<ResponseDto<Void>> leaveChatRoom(
            @PathVariable Long chatRoomId,
            @UserSession User user) {

        Long userId = user.getUserId();
        chatService.deactivateChatRoom(chatRoomId, userId);

        return ResponseEntity.ok(ResponseDto.of(null, Result.ok()));
    }

    /**
     * 관리자용 모든 채팅방 조회
     */
    @Override
    @GetMapping("/admin/rooms")
    public ResponseEntity<ResponseDto<List<ChatRoomDto>>> getAllChatRoomsForAdmin() {
        List<ChatRoomDto> allChatRooms = chatService.getAllChatRoomsForAdmin();
        return ResponseEntity.ok(ResponseDto.of(allChatRooms, Result.ok()));
    }

    /**
     * 텍스트 메시지 전송 (WebSocket 외에 REST API로도 전송 가능)
     */
    @Override
    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<ResponseDto<ChatMessageDto>> sendTextMessage(
            @PathVariable Long chatRoomId,
            @RequestParam String content,
            @UserSession User user) {

        Long senderId = user.getUserId();
        ChatMessageDto messageDto = chatService.sendMessage(chatRoomId, senderId, content, MessageType.TEXT);

        return ResponseEntity.ok(ResponseDto.of(messageDto, Result.ok()));
    }

    /**
     * 이미지 메시지 전송
     */
    @Override
    @PostMapping("/rooms/{chatRoomId}/images")
    public ResponseEntity<ResponseDto<ChatMessageDto>> sendImageMessage(
            @PathVariable Long chatRoomId,
            @RequestParam String imageUrl,
            @UserSession User user) {

        Long senderId = user.getUserId();
        ChatMessageDto messageDto = chatService.sendMessage(chatRoomId, senderId, imageUrl, MessageType.IMAGE);

        return ResponseEntity.ok(ResponseDto.of(messageDto, Result.ok()));
    }

    @Override
    @PostMapping("/image")
    public ResponseEntity<ResponseDto<List<ChatImageResponseDto>>> postChatImage(
            @RequestPart("file") MultipartFile[] multipartFile,
            @UserSession User user) {

        List<ChatImageResponseDto> response = chatService.createChatImage(multipartFile);
        return ResponseEntity.ok(ResponseDto.of(response, Result.ok()));
    }
}