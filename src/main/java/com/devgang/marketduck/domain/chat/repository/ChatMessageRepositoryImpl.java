package com.devgang.marketduck.domain.chat.repository;

import com.devgang.marketduck.domain.chat.entity.ChatMessage;
import com.devgang.marketduck.domain.chat.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public Page<ChatMessage> findRecentMessagesByChatRoom(ChatRoom chatRoom, int limit) {
        return chatMessageJpaRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, PageRequest.of(0, limit));
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