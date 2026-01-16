package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final ChannelRepository channelRepository;
  private final UserRepository userRepository;
  private final MessageMapper messageMapper;

  @Override
  @Transactional
  public MessageDto createMessage(MessageCreateRequest request) {

    // 채널, 유저 존재 검사
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(
            () -> new NoSuchElementException("Channel을 찾을 수 없습니다. " + request.channelId()));

    User author = userRepository.findById(request.authorId())
        .orElseThrow(() -> new NoSuchElementException("User를 찾을 수 없습니다. " + request.authorId()));

    // 첨부파일 엔티티 - CascadeType.ALL 설정 -> save 호출 안해도 됨
    List<BinaryContent> attachments = request.attachments().stream()
        .map(req -> new BinaryContent(req.fileName(), req.size(), req.contentType(), req.bytes()))
        .toList();

    // Message 엔티티 생성 및 저장
    Message message = new Message(author, channel, request.content(), attachments);
    Message savedMessage = messageRepository.save(message);

    return messageMapper.toDto(savedMessage);
  }

  @Override
  public MessageDto findMessage(UUID id) {
    return messageRepository.findById(id)
        .map(messageMapper::toDto)
        .orElseThrow(() -> new NoSuchElementException("Message를 찾을 수 없습니다. " + id));
  }

  @Override
  public List<MessageDto> findAllByChannelId(UUID channelId) {
    return messageRepository.findAllByChannel_Id(channelId).stream()
        .map(messageMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  public MessageDto updateMessage(UUID messageId, MessageUpdateRequest request) {
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new NoSuchElementException("Message를 찾을 수 없습니다. " + messageId));

    message.update(request.newContent());

    return messageMapper.toDto(message);
  }

  @Override
  @Transactional
  public void deleteMessage(UUID id) {
    if (!messageRepository.existsById(id)) {
      throw new NoSuchElementException("Message를 찾을 수 없습니다. " + id);
    }

    //  CascadeType.ALL 및 orphanRemoval=true 설정
    // 연관된 BinaryContent도 DB에서 자동으로 삭제됨
    messageRepository.deleteById(id);
  }
}
