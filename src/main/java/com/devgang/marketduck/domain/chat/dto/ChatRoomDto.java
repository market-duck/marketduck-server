package com.devgang.marketduck.domain.chat.dto;

import com.devgang.marketduck.api.user.dto.UserSimpleResponseDto;
import com.devgang.marketduck.constant.ChatRoomStatus;
import com.devgang.marketduck.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private Long chatRoomId;
    private String sessionId;
    private Long feedId;
    private String feedTitle;
    private String feedImageUrl;
    private UserSimpleResponseDto sender;
    private UserSimpleResponseDto receiver;
    private ChatRoomStatus status;
    private LocalDateTime createdAt;
    private List<ChatMessageDto> recentMessages;
    private long unreadCount;

    // 엔티티 -> DTO 변환 (최근 메시지 목록 없이)
    public static ChatRoomDto of(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .chatRoomId(chatRoom.getChatRoomId())
                .sessionId(chatRoom.getSessionId())
                .feedId(chatRoom.getFeed().getFeedId())
                .feedTitle(chatRoom.getFeed().getTitle())
                .feedImageUrl(chatRoom.getFeed().getFeedImages().isEmpty() ? null
                        : chatRoom.getFeed().getFeedImages().iterator().next().getFileUrl())
                .sender(UserSimpleResponseDto.of(chatRoom.getSender()))
                .receiver(UserSimpleResponseDto.of(chatRoom.getReceiver()))
                .status(chatRoom.getStatus())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    // 엔티티 -> DTO 변환 (최근 메시지 목록 포함)
    public static ChatRoomDto of(ChatRoom chatRoom, List<ChatMessageDto> recentMessages, long unreadCount) {
        ChatRoomDto dto = of(chatRoom);
        dto.setRecentMessages(recentMessages);
        dto.setUnreadCount(unreadCount);
        return dto;
    }
}