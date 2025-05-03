package com.devgang.marketduck.domain.chat.entity;

import com.devgang.marketduck.audit.Auditable;
import com.devgang.marketduck.constant.MessageType;
import com.devgang.marketduck.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    // 읽음 여부
    @Column(nullable = false)
    private boolean isRead;

    // 일반 텍스트 메시지 생성 메서드
    public static ChatMessage createTextMessage(User sender, ChatRoom chatRoom, String content) {
        return ChatMessage.builder()
                .content(content)
                .sender(sender)
                .chatRoom(chatRoom)
                .messageType(MessageType.TEXT)
                .isRead(false)
                .build();
    }

    // 이미지 메시지 생성 메서드
    public static ChatMessage createImageMessage(User sender, ChatRoom chatRoom, String imageUrl) {
        return ChatMessage.builder()
                .content(imageUrl)
                .sender(sender)
                .chatRoom(chatRoom)
                .messageType(MessageType.IMAGE)
                .isRead(false)
                .build();
    }

    // 시스템 메시지 생성 메서드
    public static ChatMessage createSystemMessage(ChatRoom chatRoom, String content) {
        return ChatMessage.builder()
                .content(content)
                .chatRoom(chatRoom)
                .messageType(MessageType.SYSTEM)
                .isRead(true)
                .build();
    }
}