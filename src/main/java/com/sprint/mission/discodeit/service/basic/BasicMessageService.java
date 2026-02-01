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
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    User author = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException(userId + " 해당하는 유저를 찾을 수 없습니다."));
    Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new NoSuchElementException(channelId + " 해당하는 채널을 찾을 수 없습니다."));

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
    return messageMapper.toDto(savedMessage);
  }

  @Override
  public MessageDto findById(UUID messageId) {
    return messageRepository.findById(messageId)
        .map(messageMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException(messageId + " 해당하는 메시지가 존재하지 않습니다."));
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

    return pageResponseMapper.fromSlice(
        messageDtoSlice,
        MessageDto::createdAt
    );
  }

  @Override
  @Transactional
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new NoSuchElementException(messageId + " 해당하는 메시지가 존재하지 않습니다."));

    message.update(request.newContent());
    return messageMapper.toDto(messageRepository.save(message));
  }

  @Override
  @Transactional
  public void delete(UUID messageId) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new NoSuchElementException(messageId + " 해당하는 메시지가 존재하지 않습니다."));

    messageRepository.delete(message);
  }
}
