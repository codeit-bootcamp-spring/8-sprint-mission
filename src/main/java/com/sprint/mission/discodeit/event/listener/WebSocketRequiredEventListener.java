package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final MessageRepository messageRepository;
  private final MessageMapper messageMapper;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMessage(MessageCreatedEvent event) {
    UUID messageId = event.messageId();
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> MessageNotFoundException.withId(messageId));

    String destination = "/sub/channels." + event.channelId() + ".messages";
    messagingTemplate.convertAndSend(destination, messageMapper.toDto(message));
  }
}
