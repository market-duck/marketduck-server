package com.devgang.marketduck.domain.chat.service;

import com.devgang.marketduck.constant.AwsProperty;
import com.devgang.marketduck.constant.ChatRoomStatus;
import com.devgang.marketduck.constant.ErrorCode;
import com.devgang.marketduck.constant.MessageType;
import com.devgang.marketduck.domain.chat.dto.ChatMessageDto;
import com.devgang.marketduck.domain.chat.dto.ChatRoomDto;
import com.devgang.marketduck.domain.chat.entity.ChatMessage;
import com.devgang.marketduck.domain.chat.entity.ChatRoom;
import com.devgang.marketduck.domain.chat.repository.ChatMessageRepository;
import com.devgang.marketduck.domain.chat.repository.ChatRoomRepository;
import com.devgang.marketduck.domain.feed.entity.Feed;
import com.devgang.marketduck.domain.feed.repository.FeedRepository;
import com.devgang.marketduck.domain.user.entity.User;
import com.devgang.marketduck.domain.user.repository.UserRepository;
import com.devgang.marketduck.exception.ServiceLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.devgang.marketduck.file.service.FileService;
import com.devgang.marketduck.domain.chat.dto.ChatImageResponseDto;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FileService fileService;

    // 채팅방 생성
    public ChatRoomDto createChatRoom(Long feedId, Long senderId, Long receiverId) {
        // 사용자 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));

        // 피드 조회
        Feed feed = feedRepository.findFeedById(feedId);

        // 본인과의 채팅방은 생성 불가
        if (senderId.equals(receiverId)) {
            throw new ServiceLogicException(ErrorCode.INVALID_PARAMETER, "자신과 채팅할 수 없습니다.");
        }

        // 이미 존재하는 채팅방 확인
        ChatRoom existingChatRoom = chatRoomRepository.findByFeedAndSenderAndReceiver(feed, sender, receiver)
                .orElse(null);

        if (existingChatRoom != null) {
            if (existingChatRoom.getStatus() == ChatRoomStatus.INACTIVE) {
                // 비활성화된 채팅방이 있으면 다시 활성화
                existingChatRoom.setStatus(ChatRoomStatus.ACTIVE);
                chatRoomRepository.save(existingChatRoom);
            }
            return getChatRoomWithMessages(existingChatRoom.getChatRoomId(), senderId);
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.create(feed, sender, receiver);
        chatRoom = chatRoomRepository.save(chatRoom);

        // 시스템 메시지 생성
        ChatMessage systemMessage = ChatMessage.createSystemMessage(
                chatRoom,
                String.format("[%s] 피드에 대한 채팅방이 개설되었습니다.", feed.getTitle()));
        chatMessageRepository.save(systemMessage);

        // Redis 토픽 생성
        createRedisTopic(chatRoom.getSessionId());

        return getChatRoomWithMessages(chatRoom.getChatRoomId(), senderId);
    }

    // 채팅방 목록 조회
    public List<ChatRoomDto> getChatRooms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));

        List<ChatRoom> chatRooms = chatRoomRepository.findByUserAndStatus(user, ChatRoomStatus.ACTIVE);

        return chatRooms.stream()
                .map(chatRoom -> {
                    // 각 채팅방의 최근 메시지 1개와 읽지 않은 메시지 수를 함께 조회
                    List<ChatMessage> recentMessages = chatMessageRepository.findRecentMessagesByChatRoom(chatRoom, 1);
                    List<ChatMessageDto> messageDtos = recentMessages.stream()
                            .map(ChatMessageDto::of)
                            .collect(Collectors.toList());

                    long unreadCount = recentMessages.stream()
                            .filter(message -> !message.isRead() &&
                                    (message.getSender() == null ||
                                            !message.getSender().getUserId().equals(userId)))
                            .count();

                    return ChatRoomDto.of(chatRoom, messageDtos, unreadCount);
                })
                .collect(Collectors.toList());
    }

    // 채팅방 상세 조회
    public ChatRoomDto getChatRoomWithMessages(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_CHAT_ROOM));

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.getSender().getUserId().equals(userId) &&
                !chatRoom.getReceiver().getUserId().equals(userId)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED, "접근 권한이 없는 채팅방입니다.");
        }

        // 최근 메시지 30개 조회
        List<ChatMessage> recentMessages = chatMessageRepository.findRecentMessagesByChatRoom(chatRoom, 30);
        List<ChatMessageDto> messageDtos = recentMessages.stream()
                .map(ChatMessageDto::of)
                .collect(Collectors.toList());

        // 읽지 않은 메시지 수 계산
        long unreadCount = recentMessages.stream()
                .filter(message -> !message.isRead() &&
                        (message.getSender() != null &&
                                !message.getSender().getUserId().equals(userId)))
                .count();

        // 읽지 않은 메시지 읽음 처리
        for (ChatMessage message : recentMessages) {
            if (!message.isRead() &&
                    (message.getSender() == null || !message.getSender().getUserId().equals(userId))) {
                message.setRead(true);
                chatMessageRepository.save(message);
            }
        }

        // Redis 토픽 생성(없는 경우에만)
        createRedisTopic(chatRoom.getSessionId());

        return ChatRoomDto.of(chatRoom, messageDtos, unreadCount);
    }

    // 채팅 메시지 전송
    public ChatMessageDto sendMessage(Long chatRoomId, Long senderId, String content, MessageType messageType) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_CHAT_ROOM));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_USER));

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.getSender().getUserId().equals(senderId) &&
                !chatRoom.getReceiver().getUserId().equals(senderId)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED, "메시지를 보낼 권한이 없습니다.");
        }

        // 메시지 타입에 따라 메시지 생성
        ChatMessage message;
        if (messageType == MessageType.TEXT) {
            message = ChatMessage.createTextMessage(sender, chatRoom, content);
        } else if (messageType == MessageType.IMAGE) {

            message = ChatMessage.createImageMessage(sender, chatRoom, content);
        } else {
            throw new ServiceLogicException(ErrorCode.INVALID_PARAMETER, "지원하지 않는 메시지 타입입니다.");
        }

        // 메시지 저장
        message = chatMessageRepository.save(message);

        // DTO 변환
        ChatMessageDto messageDto = ChatMessageDto.of(message);

        // Redis로 메시지 발행
        redisTemplate.convertAndSend("chat." + chatRoom.getSessionId(), messageDto);

        return messageDto;
    }

    // 채팅방 비활성화
    public void deactivateChatRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ServiceLogicException(ErrorCode.NOT_FOUND_CHAT_ROOM));

        // 사용자가 채팅방 참여자인지 확인
        if (!chatRoom.getSender().getUserId().equals(userId) &&
                !chatRoom.getReceiver().getUserId().equals(userId)) {
            throw new ServiceLogicException(ErrorCode.ACCESS_DENIED, "채팅방을 비활성화할 권한이 없습니다.");
        }

        // 채팅방 상태 업데이트
        chatRoom.setStatus(ChatRoomStatus.INACTIVE);
        chatRoomRepository.save(chatRoom);
    }

    // 관리자용 모든 채팅방 조회
    public List<ChatRoomDto> getAllChatRoomsForAdmin() {
        List<ChatRoom> allChatRooms = chatRoomRepository.findAllChatRooms();

        return allChatRooms.stream()
                .map(ChatRoomDto::of)
                .collect(Collectors.toList());
    }

    // Redis 토픽 생성
    private void createRedisTopic(String sessionId) {
        // 이미 토픽이 존재하는지 확인하는 로직은 생략
        // Redis에 토픽 키가 있는지 확인하는 코드를 추가할 수 있음

        // 채널 토픽 생성
        new ChannelTopic("chat." + sessionId);
        log.info("Redis topic created: chat.{}", sessionId);
    }

    public List<ChatImageResponseDto> createChatImage(MultipartFile[] files) {
        if (files.length == 0) {
            throw new ServiceLogicException(ErrorCode.INVALID_PARAMETER, "이미지 파일이 없습니다.");
        } else if (files.length > 5) {
            throw new ServiceLogicException(ErrorCode.INVALID_PARAMETER, "이미지 파일은 최대 5개까지만 업로드 가능합니다.");
        }

        List<ChatImageResponseDto> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!fileService.isImageFile(file)) {
                throw new ServiceLogicException(ErrorCode.INVALID_FILE_TYPE, "이미지 파일만 업로드 가능합니다.");
            }

            String fileName = UUID.randomUUID().toString();
            String url = fileService.saveMultipartFileForAws(file, AwsProperty.CHAT_IMAGE, fileName);
            imageUrls.add(ChatImageResponseDto.builder().imageUrl(url).build());
        }

        return imageUrls;
    }
}