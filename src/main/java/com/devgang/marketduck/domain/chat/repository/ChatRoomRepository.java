package com.devgang.marketduck.domain.chat.repository;

import com.devgang.marketduck.constant.ChatRoomStatus;
import com.devgang.marketduck.domain.chat.entity.ChatRoom;
import com.devgang.marketduck.domain.feed.entity.Feed;
import com.devgang.marketduck.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {
    ChatRoom save(ChatRoom chatRoom);

    Optional<ChatRoom> findById(Long id);

    Optional<ChatRoom> findBySessionId(String sessionId);

    // 피드에 대한 특정 사용자 간의 채팅방 조회
    Optional<ChatRoom> findByFeedAndSenderAndReceiver(Feed feed, User sender, User receiver);

    // 사용자가 참여한 모든 활성화된 채팅방 조회
    List<ChatRoom> findByUserAndStatus(User user, ChatRoomStatus status);

    // 특정 피드에 대한 모든 채팅방 조회 (관리자용)
    List<ChatRoom> findAllByFeed(Feed feed);

    // 채팅방 상태 업데이트
    void updateStatus(Long chatRoomId, ChatRoomStatus status);

    // 관리자용 모든 채팅방 조회
    List<ChatRoom> findAllChatRooms();
}