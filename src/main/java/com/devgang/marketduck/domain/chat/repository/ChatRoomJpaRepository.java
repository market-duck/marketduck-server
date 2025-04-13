package com.devgang.marketduck.domain.chat.repository;

import com.devgang.marketduck.constant.ChatRoomStatus;
import com.devgang.marketduck.domain.chat.entity.ChatRoom;
import com.devgang.marketduck.domain.feed.entity.Feed;
import com.devgang.marketduck.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findBySessionId(String sessionId);

    Optional<ChatRoom> findByFeedAndSenderAndReceiver(Feed feed, User sender, User receiver);

    @Query("SELECT cr FROM ChatRoom cr WHERE (cr.sender = :user OR cr.receiver = :user) AND cr.status = :status")
    List<ChatRoom> findByUserAndStatus(@Param("user") User user, @Param("status") ChatRoomStatus status);

    List<ChatRoom> findAllByFeed(Feed feed);

    @Modifying
    @Query("UPDATE ChatRoom cr SET cr.status = :status WHERE cr.chatRoomId = :chatRoomId")
    void updateStatus(@Param("chatRoomId") Long chatRoomId, @Param("status") ChatRoomStatus status);

    // 모든 채팅방 조회 (정렬: 최신순)
    @Query("SELECT cr FROM ChatRoom cr ORDER BY cr.createdAt DESC")
    List<ChatRoom> findAllChatRooms();
}