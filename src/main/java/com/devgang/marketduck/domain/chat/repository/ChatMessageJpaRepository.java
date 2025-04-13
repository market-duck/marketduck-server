package com.devgang.marketduck.domain.chat.repository;

import com.devgang.marketduck.domain.chat.entity.ChatMessage;
import com.devgang.marketduck.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom = :chatRoom ORDER BY cm.createdAt DESC LIMIT :limit")
    List<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(@Param("chatRoom") ChatRoom chatRoom,
            @Param("limit") int limit);

    List<ChatMessage> findAllByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.messageId = :messageId")
    void markAsRead(@Param("messageId") Long messageId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.receiver.userId = :userId AND cm.isRead = false")
    long countUnreadMessages(@Param("userId") Long userId);
}