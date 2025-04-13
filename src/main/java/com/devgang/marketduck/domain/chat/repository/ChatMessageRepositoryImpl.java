package com.devgang.marketduck.domain.chat.repository;

import com.devgang.marketduck.domain.chat.entity.ChatMessage;
import com.devgang.marketduck.domain.chat.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final ChatMessageJpaRepository chatMessageJpaRepository;

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageJpaRepository.save(chatMessage);
    }

    @Override
    public Optional<ChatMessage> findById(Long id) {
        return chatMessageJpaRepository.findById(id);
    }

    @Override
    public List<ChatMessage> findRecentMessagesByChatRoom(ChatRoom chatRoom, int limit) {
        return chatMessageJpaRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom, limit);
    }

    @Override
    public List<ChatMessage> findAllByChatRoom(ChatRoom chatRoom) {
        return chatMessageJpaRepository.findAllByChatRoomOrderByCreatedAtDesc(chatRoom);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        chatMessageJpaRepository.markAsRead(messageId);
    }

    @Override
    public long countUnreadMessages(Long userId) {
        return chatMessageJpaRepository.countUnreadMessages(userId);
    }
}