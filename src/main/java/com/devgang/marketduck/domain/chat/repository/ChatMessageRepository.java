package com.devgang.marketduck.domain.chat.repository;

import com.devgang.marketduck.domain.chat.entity.ChatMessage;
import com.devgang.marketduck.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
public interface ChatMessageRepository {
    ChatMessage save(ChatMessage chatMessage);

    Optional<ChatMessage> findById(Long id);

    // 특정 채팅방의 최근 메시지 N개 조회
    Page<ChatMessage> findRecentMessagesByChatRoom(ChatRoom chatRoom, int limit);

    // 특정 채팅방의 모든 메시지 조회
    List<ChatMessage> findAllByChatRoom(ChatRoom chatRoom);

    // 메시지 읽음 처리
    void markAsRead(Long messageId);

    // 특정 사용자의 읽지 않은 메시지 수 조회
    long countUnreadMessages(Long userId);
}