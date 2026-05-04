package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void on(MessageCreatedEvent event) {
        sendKafka(event);
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void on(RoleUpdatedEvent event) {
        sendKafka(event);
    }

    @Async("notificationTaskExecutor")
    @EventListener
    public void on(S3UploadFailedEvent event) {
        sendKafka(event);
    }

    private <T> void sendKafka(T event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String topic = "discodeit.".concat(event.getClass().getSimpleName());

            kafkaTemplate.send(topic, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("[Kafka 전송 실패] Topic: {}, Reason: {}",
                                    topic, ex.getMessage());
                        } else {
                            log.info("[Kafka 전송 성공] Topic: {}, Offset: {}",
                                    topic, result.getRecordMetadata().offset());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("[Kafka 직렬화 실패], Event: {}", event.getClass().getSimpleName(), e);
        }
    }
}
