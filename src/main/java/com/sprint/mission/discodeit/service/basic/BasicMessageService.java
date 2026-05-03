package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;
    private final BinaryContentRepository binaryContentRepository;
    private final PageResponseMapper pageResponseMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 메시지 ?성 (채널/?성???으??외).
     */
    @Transactional
    @Override
    public MessageDto create(MessageCreateRequest messageCreateRequest,
                             List<BinaryContentCreateRequest> binaryContentCreateRequests) {
        long wallStartNs = System.nanoTime();
        UUID channelId = messageCreateRequest.channelId();
        UUID authorId = messageCreateRequest.authorId();
        log.debug("메시지 생성, channelId={}, authorId={}, attachments={}",
                channelId, authorId, binaryContentCreateRequests.size());

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> {
                    log.warn("메시지 생성 실패: 채널 없음, channelId={}", channelId);
                    return new NoSuchElementException(
                            "Channel with id " + channelId + " does not exist");
                });
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> {
                    log.warn("메시지 생성 실패: 작성자 없음, authorId={}", authorId);
                    return new NoSuchElementException("Author with id " + authorId + " does not exist");
                });

        List<BinaryContent> attachments = binaryContentCreateRequests.stream()
                .map(attachmentRequest -> {
                    String fileName = attachmentRequest.fileName();
                    String contentType = attachmentRequest.contentType();
                    byte[] bytes = attachmentRequest.bytes();

                    BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
                            contentType);
                    binaryContentRepository.save(binaryContent);
                    applicationEventPublisher.publishEvent(new BinaryContentCreatedEvent(binaryContent.getId(), bytes));
                    return binaryContent;
                })
                .toList();

        String content = messageCreateRequest.content();
        Message message = new Message(
                content,
                channel,
                author,
                attachments
        );

        messageRepository.save(message);
        applicationEventPublisher.publishEvent(
                new MessageCreatedEvent(
                        message.getId(),
                        channel.getId(),
                        author.getId(),
                        author.getUsername(),
                        channelDisplayName(channel),
                        content != null ? content : ""));
        long wallMs = (System.nanoTime() - wallStartNs) / 1_000_000L;
        log.info(
                "메시지 생성 완료, messageId={}, channelId={}, authorId={}, wallTimeMs={} (첨부 {}건의 바이너리 저장은 AFTER_COMMIT 비동기 리스너에서 수행되며, "
                        + "동기로 리스너까지 기다리면 첨부당 약 3초 지연이 그대로 API 응답 시간에 합산됩니다.)",
                message.getId(), channelId, authorId, wallMs, binaryContentCreateRequests.size());
        return messageMapper.toDto(message);
    }

    @Transactional(readOnly = true)
    @Override
    public MessageDto find(UUID messageId) {
        return messageRepository.findById(messageId)
                .map(messageMapper::toDto)
                .orElseThrow(
                        () -> new NoSuchElementException("Message with id " + messageId + " not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant createAt,
                                                       Pageable pageable) {
        Instant cursor = Optional.ofNullable(createAt).orElse(Instant.now());
        Slice<MessageDto> slice = messageRepository.findAllByChannelIdWithAuthor(
                        channelId,
                        cursor,
                        pageable)
                .map(messageMapper::toDto);
        List<MessageDto> filtered = slice.getContent().stream()
                .filter(dto -> channelId.equals(dto.channelId()) || channelId.equals(dto.channel()))
                .toList();
        // Front clients may send stale cursor when switching channels.
        // If that happens, fallback to latest page for the selected channel.
        if (filtered.isEmpty() && createAt != null) {
            slice = messageRepository.findAllByChannelIdWithAuthor(channelId, Instant.now(), pageable)
                    .map(messageMapper::toDto);
            filtered = slice.getContent().stream()
                    .filter(dto -> channelId.equals(dto.channelId()) || channelId.equals(dto.channel()))
                    .toList();
        }
        Instant nextCursor = filtered.isEmpty() ? null : filtered.get(filtered.size() - 1).createdAt();
        log.debug("메시지 조회, channelId={}, requestedSize={}, returnedSize={}",
                channelId, slice.getContent().size(), filtered.size());
        return new PageResponse<>(
                filtered,
                nextCursor,
                slice.getSize(),
                slice.hasNext(),
                null
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<MessageDto> findAllByChannelIdAsList(UUID channelId) {
        Pageable pageable = PageRequest.of(0, 500, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<MessageDto> results = messageRepository.findAllByChannelIdWithAuthor(channelId, Instant.now(), pageable)
                .map(messageMapper::toDto)
                .getContent()
                .stream()
                .filter(dto -> channelId.equals(dto.channelId()) || channelId.equals(dto.channel()))
                .toList();
        log.debug("메시지 목록 조회, channelId={}, returnedSize={}", channelId, results.size());
        return results;
    }

    /**
     * 메시지 수정 (대상 없으면 예외).
     */
    @Transactional
    @Override
    @PreAuthorize("@securityExpressionService.isMessageAuthor(#messageId, authentication)")
    public MessageDto update(UUID messageId, MessageUpdateRequest request) {
        String newContent = request.newContent();
        log.debug("메시지 수정, messageId={}", messageId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> {
                    log.warn("메시지 수정 실패: 메시지 없음, messageId={}", messageId);
                    return new NoSuchElementException("Message with id " + messageId + " not found");
                });
        message.update(newContent);
        log.info("메시지 수정 완료, messageId={}", messageId);
        return messageMapper.toDto(message);
    }

    /**
     * 메시지 삭제 (대상 없으면 예외).
     */
    @Transactional
    @Override
    @PreAuthorize("@securityExpressionService.isMessageAuthor(#messageId, authentication)")
    public void delete(UUID messageId) {
        log.debug("메시지 삭제 시도, messageId={}", messageId);
        if (!messageRepository.existsById(messageId)) {
            log.warn("메시지 삭제 실패: 메시지 없음, messageId={}", messageId);
            throw new NoSuchElementException("Message with id " + messageId + " not found");
        }
        messageRepository.deleteById(messageId);
        log.info("메시지 삭제 완료, messageId={}", messageId);
    }

    private static String channelDisplayName(Channel channel) {
        String name = channel.getName();
        return (name != null && !name.isBlank()) ? name : "채널";
    }
}
