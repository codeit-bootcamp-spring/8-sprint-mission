package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.dto.MessageDto;
import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelExcption.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.MessageException.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserException.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;

  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;

  @Override
  @Transactional
  public MessageDto create(MessageCreateRequest request,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    UUID userId = request.authorId();
    UUID channelId = request.channelId();
    String content = request.content();

    log.info("Service: 메시지 생성 요청 - content: {}", content);
    User author = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("Service: 메시지 생성 실패(존재하지 않는 유저) - ID: {}", userId);
          return new UserNotFoundException(userId);
        });

    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> {
          log.warn("Service: 메시지 생성 실패(존재하지 않는 채널) - ID: {}", channelId);
          return new ChannelNotFoundException(channelId);
        });

    List<BinaryContentCreateRequest> fileRequests =
        binaryContentCreateRequests != null ? binaryContentCreateRequests : Collections.emptyList();

    List<BinaryContent> attachments = fileRequests.stream()
        .map(fileRequest -> {
              BinaryContent binaryContent = new BinaryContent(
                  fileRequest.fileName(),
                  (long) fileRequest.bytes().length,
                  fileRequest.contentType()
              );
              binaryContentRepository.save(binaryContent);

              binaryContentStorage.put(binaryContent.getId(), fileRequest.bytes());
              log.debug("Service: 메시지 첨부파일 업로드 성공 - ID: {}", binaryContent.getId());
              return binaryContent;
            }
        )
        .toList();

    Message message = new Message(
        request.content(),
        channel,
        author,
        attachments
    );

    Message savedMessage = messageRepository.save(message);
    log.info("Service: 메시지 생성 성공 - ID: {}", savedMessage.getId());
    return messageMapper.toDto(savedMessage);
  }

  @Override
  public MessageDto findById(UUID messageId) {
    return messageRepository.findById(messageId)
        .map(messageMapper::toDto)
        .orElseThrow(() -> new MessageNotFoundException(messageId));
  }

  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor,
      Pageable pageable) {
    Pageable fixedPageable = PageRequest.of(
        0,
        pageable.getPageSize(),
        pageable.getSort()
    );

    Slice<Message> messageSlice = messageRepository.findAllByChannelIdWithCursor(
        channelId,
        cursor,
        fixedPageable
    );

    Slice<MessageDto> messageDtoSlice = messageSlice.map(messageMapper::toDto);

    Instant nextCursor = messageSlice.isEmpty() ? null :
        messageSlice.getContent().get(messageSlice.getContent().size() - 1).getCreatedAt();

    return pageResponseMapper.fromSlice(
        messageDtoSlice,
        MessageDto::createdAt
    );
  }

  @Override
  @Transactional
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    log.info("Service: 메시지 수정 요청 - ID: {}", messageId);
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn("Service: 메시지 수정 실패(존재하지 않는 메시지) - ID: {}", messageId);
          return new MessageNotFoundException(messageId);
        });

    message.update(request.newContent());
    Message savedMessage = messageRepository.save(message);
    log.info("Service: 메시지 수정 완료 - ID: {}", messageId);
    return messageMapper.toDto(savedMessage);
  }

  @Override
  @Transactional
  public void delete(UUID messageId) {
    log.info("Service: 메시지 삭제 요청 - ID: {}", messageId);
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> {
          log.warn("Service: 메시지 삭제 실패(존재하지 않는 메시지) - ID: {}", messageId);
          return new MessageNotFoundException(messageId);
        });

    messageRepository.delete(message);
    log.info("Service: 메시지 삭제 완료 - ID: {}", messageId);
  }
}
