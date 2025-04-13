package com.devgang.marketduck.domain.chat.repository;

import com.devgang.marketduck.constant.ChatRoomStatus;
import com.devgang.marketduck.domain.chat.entity.ChatRoom;
import com.devgang.marketduck.domain.feed.entity.Feed;
import com.devgang.marketduck.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    private final ChatRoomJpaRepository chatRoomJpaRepository;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return chatRoomJpaRepository.save(chatRoom);
    }

    @Override
    public Optional<ChatRoom> findById(Long id) {
        return chatRoomJpaRepository.findById(id);
    }

    @Override
    public Optional<ChatRoom> findBySessionId(String sessionId) {
        return chatRoomJpaRepository.findBySessionId(sessionId);
    }

    @Override
    public Optional<ChatRoom> findByFeedAndSenderAndReceiver(Feed feed, User sender, User receiver) {
        return chatRoomJpaRepository.findByFeedAndSenderAndReceiver(feed, sender, receiver);
    }

    @Override
    public List<ChatRoom> findByUserAndStatus(User user, ChatRoomStatus status) {
        return chatRoomJpaRepository.findByUserAndStatus(user, status);
    }

    @Override
    public List<ChatRoom> findAllByFeed(Feed feed) {
        return chatRoomJpaRepository.findAllByFeed(feed);
    }

    @Override
    @Transactional
    public void updateStatus(Long chatRoomId, ChatRoomStatus status) {
        chatRoomJpaRepository.updateStatus(chatRoomId, status);
    }

    @Override
    public List<ChatRoom> findAllChatRooms() {
        return chatRoomJpaRepository.findAllChatRooms();
    }
}