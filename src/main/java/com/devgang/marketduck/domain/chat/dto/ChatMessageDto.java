package com.devgang.marketduck.domain.chat.dto;

import com.devgang.marketduck.constant.MessageType;
import com.devgang.marketduck.domain.chat.entity.ChatMessage;
import com.devgang.marketduck.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long messageId;
    private String content;
    private Long senderId;
    private String senderNickname;
    private String senderProfileImage;
    private Long chatRoomId;
    private String sessionId;
    private MessageType messageType;
    private boolean isRead;
    private LocalDateTime createdAt;

    // 엔티티 -> DTO 변환
    public static ChatMessageDto of(ChatMessage message) {
        return ChatMessageDto.builder()
                .messageId(message.getMessageId())
                .content(message.getContent())
                .senderId(message.getSender() != null ? message.getSender().getUserId() : null)
                .senderNickname(message.getSender() != null ? message.getSender().getNickname() : "시스템")
                .senderProfileImage(message.getSender() != null ? message.getSender().getProfileImageUrl() : null)
                .chatRoomId(message.getChatRoom().getChatRoomId())
                .sessionId(message.getChatRoom().getSessionId())
                .messageType(message.getMessageType())
                .isRead(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    // 채팅방에 메시지 전송을 위한 DTO 생성
    public static ChatMessageDto createSendMessageDto(
            String content,
            Long chatRoomId,
            String sessionId,
            User sender,
            MessageType messageType) {

        return ChatMessageDto.builder()
                .content(content)
                .senderId(sender.getUserId())
                .senderNickname(sender.getNickname())
                .senderProfileImage(sender.getProfileImageUrl())
                .chatRoomId(chatRoomId)
                .sessionId(sessionId)
                .messageType(messageType)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}