package com.sprint.mission.discodeit.event.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.Sse.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserDeletedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserUpdatedEvent;
import com.sprint.mission.discodeit.event.UserLoginOutEvent;
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

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(MessageCreatedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(RoleUpdatedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(S3UploadFailedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(NotificationCreatedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(ChannelCreatedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(ChannelUpdatedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(ChannelDeletedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(UserCreatedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(UserUpdatedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener
    public void on(UserDeletedEvent event) {
        sendKafka(event);
    }

    @Async("asyncTaskExecutor")
    @EventListener
    public void on(UserLoginOutEvent event) {
        sendKafka(event);
    }

    private <T> void sendKafka(T event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("discodeit.".concat(event.getClass().getSimpleName()), payload);
        } catch (JsonProcessingException e) {
            log.error("Kafka 전송 중 오류 발생", e);
            throw new RuntimeException(e);
        }
    }
}
