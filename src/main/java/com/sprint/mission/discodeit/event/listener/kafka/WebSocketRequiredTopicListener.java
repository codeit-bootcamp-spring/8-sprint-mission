package com.sprint.mission.discodeit.event.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.dto.MessageDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "discodeit.MessageCreatedEvent",
            groupId = "${spring.kafka.consumer.group-id}-web-${random.uuid}"
    )
    public void onMessageCreatedEvent(String kafkaEvent) {
        try {
            MessageCreatedEvent event = objectMapper.readValue(
                    kafkaEvent,
                    MessageCreatedEvent.class
            );

            MessageDto message = event.getData();
            UUID channelId = message.channelId();

            // 전송할 엔드포인트 생성
            String destination = "/sub/channels." + channelId + ".messages";

            // 구독자들에게 메시지 전송
            messagingTemplate.convertAndSend(destination, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
