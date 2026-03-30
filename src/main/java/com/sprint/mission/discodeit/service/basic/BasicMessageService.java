package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveFailedException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final MessageRepository messageRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final MessageMapper messageMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final PageResponseMapper pageResponseMapper;

  public boolean isAuthor(UUID messageId, UUID userId) {
    return messageRepository.findById(messageId)
        .map(message -> message.getAuthor().getId().equals(userId))
        .orElse(false);
  }

  @Transactional
  @Override
  public MessageDto create(MessageCreateRequest messageCreateRequest,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    log.info("[MessageService] 메시지 생성 시작 -  채널 Id: {}, 작성자 Id: {}",
        messageCreateRequest.channelId(), messageCreateRequest.authorId());

    Channel channel = channelRepository.findById(messageCreateRequest.channelId())
        .orElseThrow(
            () -> {
              log.warn("[MessageService] 메시지 생성 실패 - 존재하지 않는 채널 Id: {}",
                  messageCreateRequest.channelId());
              return new ChannelNotFoundException(messageCreateRequest.channelId());
            });

    User author = userRepository.findById(messageCreateRequest.authorId())
        .orElseThrow(
            () -> {
              log.warn("[MessageService] 메시지 생성 실패 - 존재하지 않는 작성자 Id: {}",
                  messageCreateRequest.authorId());
              return new UserNotFoundException(messageCreateRequest.authorId());
            });

    String content = messageCreateRequest.content();

    List<BinaryContent> attachments = binaryContentCreateRequests.stream()
        .map(attachmentRequest -> {
          String fileName = attachmentRequest.fileName();
          String contentType = attachmentRequest.contentType();
          byte[] bytes = attachmentRequest.bytes();

          log.debug("[MessageService] 파일 생성 시작 - 이름: {}, 크기: {} bytes", fileName, bytes.length);
          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType);

          binaryContentRepository.save(binaryContent);

          try {
            binaryContentStorage.put(binaryContent.getId(), bytes);
          } catch (Exception e) {
            log.error("[MessageService] 파일 스토리지 저장 중 오류 발생 - 파일명: {}, 원인: {}",
                attachmentRequest.fileName(), e.getMessage());
            throw new BinaryContentSaveFailedException(binaryContent.getId(), fileName);
          }
          return binaryContent;
        })
        .toList();

    Message message = new Message(content, channel, author, attachments);
    messageRepository.save(message);

    log.info("[MessageService] 메시지 생성 완료 - Id: {}", message.getId());
    return messageMapper.toDto(message);
  }

  @Override
  public MessageDto find(UUID messageId) {
    log.debug("[MessageService] 메시지 조회 시작 - Id: {}", messageId);

    return messageRepository.findById(messageId)
        .map(messageMapper::toDto)
        .orElseThrow(() -> {
          log.warn("[MessageService] 메시지 조회 실패 - 존재하지 않는 Id: {}", messageId);
          return new MessageNotFoundException(messageId);
        });
  }

  @PreAuthorize("hasRole('ADMIN') or @basicMessageService.isAuthor(#messageId, authentication.principal.userDto.id)")
  @Transactional
  @Override
  public MessageDto update(UUID messageId, MessageUpdateRequest messageUpdateRequest) {
    log.info("[MessageService] 메시지 수정 시작 - Id: {}", messageId);

    String newContent = messageUpdateRequest.newContent();
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn("[MessageService] 메시지 수정 실패 - 존재하지 않는 Id: {}", messageId);
          return new MessageNotFoundException(messageId);
        });
    message.update(newContent);

    log.info("[MessageService] 메시지 수정 완료 - Id: {}", messageId);
    return messageMapper.toDto(message);
  }

  @PreAuthorize("hasRole('ADMIN') or @basicMessageService.isAuthor(#messageId, authentication.principal.userDto.id)")
  @Transactional
  @Override
  public void delete(UUID messageId) {
    log.info("[MessageService] 메시지 삭제 시작 - Id: {}", messageId);
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new MessageNotFoundException(messageId));

    messageRepository.delete(message);
    log.info("[MessageService] 메시지 삭제 완료 - Id: {}", messageId);
  }

  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant createdAt,
      Pageable pageable) {
    log.debug("[MessageService] 특정 채널의 메시지 목록 조회 시작 - 채널: {}, 생성 시간: {}, Size: {}",
        channelId, createdAt, pageable.getPageSize());

    Slice<MessageDto> slice = messageRepository.findAllByChannelIdWithAuthor(channelId,
            Optional.ofNullable(createdAt).orElse(Instant.now()),
            pageable)
        .map(messageMapper::toDto);

    Instant nextCursor = null;
    if (!slice.getContent().isEmpty()) {
      nextCursor = slice.getContent().get(slice.getContent().size() - 1).createdAt();
    }

    log.info("[MessageService] 특정 채널의 메시지 목록 조회 완료 - 채널: {}, 결과 수: {}, NextCursor: {}",
        channelId, slice.getContent().size(), nextCursor);
    return pageResponseMapper.fromSlice(slice, nextCursor);
  }
}
