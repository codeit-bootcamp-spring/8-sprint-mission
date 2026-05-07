package com.sprint.mission.discodeit.domain.message.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.domain.message.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(prefix = "discodeit.notification", name = "listener", havingValue = "kafka")
@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketRequiredTopicListener
{
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "discodeit.MessageCreatedEvent", groupId = "websocket-${random.uuid}")
    public void onMessageCreatedEvent(String kafkaEvent) {
        try {
            MessageCreatedEvent event = objectMapper.readValue(kafkaEvent,
                    MessageCreatedEvent.class);

            String destination = String.format("/sub/channels.%s.messages", event.channelId());
            messagingTemplate.convertAndSend(destination, event.data());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
