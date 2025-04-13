package com.devgang.marketduck.domain.chat.entity;

import com.devgang.marketduck.audit.Auditable;
import com.devgang.marketduck.constant.ChatRoomStatus;
import com.devgang.marketduck.domain.feed.entity.Feed;
import com.devgang.marketduck.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomId;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatRoomStatus status;

    @ToString.Exclude
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatMessage> messages = new LinkedHashSet<>();

    // 채팅 메시지 추가 메서드
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        message.setChatRoom(this);
    }

    // 새로운 채팅방 생성 메서드
    public static ChatRoom create(Feed feed, User sender, User receiver) {
        return ChatRoom.builder()
                .sessionId(UUID.randomUUID().toString())
                .feed(feed)
                .sender(sender)
                .receiver(receiver)
                .status(ChatRoomStatus.ACTIVE)
                .messages(new LinkedHashSet<>())
                .build();
    }
}