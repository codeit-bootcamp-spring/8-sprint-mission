package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    // 인터페이스 타입으로 주입받아 구현체와의 의존성을 분리합니다.
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final ReadStatusRepository readStatusRepository; // 개인 채널 권한 체크를 위해 주입

    @Override
    public Message create(MessageCreateRequest request) {
        // 요청 검증
        if (request.getChannelId() == null) {
            throw new IllegalArgumentException("채널 ID가 필요합니다.");
        }
        if (request.getAuthorId() == null) {
            throw new IllegalArgumentException("작성자 ID가 필요합니다.");
        }
        
        userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));
        
        Channel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));

        // 개인 채널인 경우 작성자가 참여자인지 확인
        if (channel.getType() == ChannelType.PRIVATE) {
            boolean hasAccess = readStatusRepository.findAllByUserId(request.getAuthorId()).stream()
                    .anyMatch(readStatus -> readStatus.getChannelId().equals(request.getChannelId()));
            
            if (!hasAccess) {
                throw new IllegalArgumentException("이 채널에 접근할 권한이 없습니다.");
            }
        }

        // User와 Channel 객체 조회
        com.sprint.mission.discodeit.entity.User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

        // 메시지 생성
        Message message = new Message(request.getContent(), channel, author);
        
        // 첨부파일 설정
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            List<com.sprint.mission.discodeit.entity.BinaryContent> attachments = request.getAttachmentIds().stream()
                    .map(binaryContentRepository::findById)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(java.util.stream.Collectors.toList());
            message.getAttachments().clear();
            message.getAttachments().addAll(attachments);
        }
        
        // 저장 후 채널 ID 검증
        Message savedMessage = messageRepository.save(message);
        if (!savedMessage.getChannelId().equals(request.getChannelId())) {
            throw new IllegalStateException("메시지 저장 시 채널 ID가 올바르게 설정되지 않았습니다.");
        }
        
        return savedMessage;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID가 필요합니다.");
        }
        
        List<Message> messages = messageRepository.findAllByChannelId(channelId);
        
        // 추가 검증: 모든 메시지가 해당 채널에 속하는지 확인
        return messages.stream()
                .filter(message -> message != null && message.getChannelId() != null && message.getChannelId().equals(channelId))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Message update(MessageUpdateRequest request) {
        Message message = messageRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        // 첨부파일 조회
        List<com.sprint.mission.discodeit.entity.BinaryContent> attachments = null;
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            attachments = request.getAttachmentIds().stream()
                    .map(binaryContentRepository::findById)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(java.util.stream.Collectors.toList());
        }
        
        if (attachments != null) {
            message.update(request.getContent(), attachments);
        } else {
            message.update(request.getContent());
        }
        return messageRepository.save(message);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        // 고도화: 관련된 첨부파일(BinaryContent)도 함께 삭제
        List<UUID> attachmentIds = message.getAttachmentIds();
        if (attachmentIds != null && !attachmentIds.isEmpty()) {
            attachmentIds.forEach(binaryContentRepository::delete);
        }

        messageRepository.delete(id);
    }

    @Override
    public List<MessageResponse> findByChannelId(UUID channelId) {
        // 기본 구현: 권한 체크 없이 조회 (기존 호환성 유지)
        return findAllByChannelId(channelId).stream()
                .map(this::convertToResponse)
                .toList();
    }
    
    /**
     * 채널의 메시지 조회 (권한 체크 포함)
     * 개인 채널인 경우 사용자가 참여자인지 확인
     */
    public List<MessageResponse> findByChannelId(UUID channelId, UUID userId) {
        // 입력 검증
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID가 필요합니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
        
        // 채널 존재 여부 확인
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));
        
        // 개인 채널인 경우 사용자가 참여자인지 확인
        if (channel.getType() == ChannelType.PRIVATE) {
            boolean hasAccess = readStatusRepository.findAllByUserId(userId).stream()
                    .anyMatch(readStatus -> readStatus.getChannelId().equals(channelId));
            
            if (!hasAccess) {
                throw new IllegalArgumentException("이 채널에 접근할 권한이 없습니다.");
            }
        }
        
        // 채널 ID로 메시지 조회
        List<Message> messages = findAllByChannelId(channelId);
        
        // 최종 검증: 모든 메시지가 정확히 해당 채널에 속하는지 확인
        List<Message> validMessages = messages.stream()
                .filter(message -> {
                    if (message == null || message.getChannelId() == null) {
                        return false;
                    }
                    return message.getChannelId().equals(channelId);
                })
                .collect(java.util.stream.Collectors.toList());
        
        // MessageResponse 변환 시 channelId도 명시적으로 설정
        return validMessages.stream()
                .map(message -> {
                    MessageResponse response = convertToResponse(message);
                    // 응답의 channelId가 요청한 channelId와 일치하는지 확인
                    if (!response.getChannelId().equals(channelId)) {
                        throw new IllegalStateException("메시지의 채널 ID가 일치하지 않습니다.");
                    }
                    return response;
                })
                .toList();
    }

    private MessageResponse convertToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .authorId(message.getAuthorId())
                .channelId(message.getChannelId())
                .content(message.getContent())
                .attachmentIds(message.getAttachmentIds())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}