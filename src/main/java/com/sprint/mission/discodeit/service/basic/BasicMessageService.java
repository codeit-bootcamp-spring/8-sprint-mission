package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.MessageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;

  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public MessageResponse create(MessageCreateRequest request,
      List<BinaryContentCreateRequest> binaryContentCreateRequests) {
    UUID userId = request.authorId();
    UUID channelId = request.channelId();

    userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("해당하는 유저를 찾을 수 없습니다."));
    channelRepository.findById(channelId)
        .orElseThrow(() -> new NoSuchElementException("해당하는 채널을 찾을 수 없습니다."));

    List<UUID> attachmentIds = binaryContentCreateRequests.stream()
        .map(attachmentRequest -> {
          String fileName = attachmentRequest.fileName();
          String contentType = attachmentRequest.contentType();
          byte[] bytes = attachmentRequest.bytes();

          BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
              contentType, bytes);
          BinaryContent createdBinaryContent = binaryContentRepository.save(binaryContent);
          return createdBinaryContent.getId();
        })
        .toList();

    Message message = new Message(
        request.content(),
        request.channelId(),
        request.authorId(),
        attachmentIds
    );

    Message savedMessage = messageRepository.save(message);

    return MessageResponse.from(savedMessage);
  }

  @Override
  public MessageResponse findById(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당하는 메시지를 찾을 수 없습니다."));

    return MessageResponse.from(message);
  }

  @Override
  public List<MessageResponse> findAllByChannelId(UUID id) {
    List<Message> messages = messageRepository.findAll();

    return messages.stream()
        .filter(message -> message.getChannelId().equals(id))
        .map(MessageResponse::from)
        .toList();
  }

  @Override
  public MessageResponse update(UUID id, MessageUpdateRequest request) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("수정하려는 메시지가 없습니다."));

    message.update(request.newContent());

    Message savedMessage = messageRepository.save(message);

    return MessageResponse.from(savedMessage);
  }

  @Override
  public void delete(UUID id) {
    Message message = messageRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("삭제하려는 메시지가 없습니다."));

    if (message.getAttachmentIds() != null) {
      message.getAttachmentIds().forEach(binaryContentRepository::deleteById);
    }
    messageRepository.delete(id);
  }
}
