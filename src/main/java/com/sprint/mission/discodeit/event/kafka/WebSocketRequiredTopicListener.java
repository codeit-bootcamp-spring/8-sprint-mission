package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent", groupId = "websocket-${random.uuid}")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent,
          MessageCreatedEvent.class);
      log.info("Received MessageCreatedEvent: {}", event);
      MessageDto message = event.getData();
      String destination = String.format("/sub/channels.%s.messages", message.channelId());
      messagingTemplate.convertAndSend(destination, message);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
