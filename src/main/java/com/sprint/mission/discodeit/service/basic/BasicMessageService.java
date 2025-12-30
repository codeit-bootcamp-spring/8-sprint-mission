package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public MessageResponse createMessage(MessageCreateRequest request) {
    // 채널, 유저 존재 검사
    channelRepository.findById(request.channelId())
        .orElseThrow(
            () -> new NoSuchElementException("Channel을 찾을 수 없습니다. " + request.channelId()));

    userRepository.findById(request.authorId())
        .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다. " + request.authorId()));

    // 파일을 저장
    List<UUID> attachmentIds = new ArrayList<>();
    if (request.attachments() != null) {
      for (BinaryContentCreateRequest dto : request.attachments()) {
        BinaryContent bc = new BinaryContent(
            dto.fileName(),
            dto.contentType(),
            dto.bytes(),
            request.authorId(),
            null
        );
        binaryContentRepository.save(bc);
        attachmentIds.add(bc.getId());
      }
    }

    Message message = new Message(
        request.content(),
        request.channelId(),
        request.authorId(),
        attachmentIds
    );
    messageRepository.save(message);

    return convertDto(message);
  }

  @Override
  public MessageResponse findMessage(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Message를 찾을 수 없습니다. " + id));
    return convertDto(message);
  }

  @Override
  public List<MessageResponse> findAllByChannelId(UUID channelId) {
    return messageRepository.findAllByChannelId(channelId).stream()
        .map(this::convertDto)
        .collect(Collectors.toList());
  }

  @Override
  public MessageResponse updateMessage(UUID messageId, MessageUpdateRequest request) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new NoSuchElementException("Message를 찾을 수 없습니다. " + messageId));

    message.update(request.newContent());
    messageRepository.save(message);

    return convertDto(message);
  }

  @Override
  public void deleteMessage(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Message를 찾을 수 없습니다. " + id));

    if (message.getAttachmentIds() != null && !message.getAttachmentIds().isEmpty()) {
      binaryContentRepository.deleteAllByIdIn(message.getAttachmentIds());
    }

    messageRepository.delete(id);
  }

  private MessageResponse convertDto(Message message) {
    return new MessageResponse(
        message.getId(),
        message.getChannelId(),
        message.getAuthorId(),
        message.getContent(),
        message.getCreatedAt(),
        message.getAttachmentIds()
    );
  }
}
