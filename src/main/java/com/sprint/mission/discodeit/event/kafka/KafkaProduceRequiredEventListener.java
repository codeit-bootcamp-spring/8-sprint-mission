package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.kafka.MessageCreatedKafkaEvent;
import com.sprint.mission.discodeit.dto.kafka.RoleUpdatedKafkaEvent;
import com.sprint.mission.discodeit.dto.kafka.S3UploadFailedKafkaEvent;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(name = "discodeit.notification.mode", havingValue = "kafka")
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) throws JsonProcessingException {
    Message message = event.getMessage();
    MessageCreatedKafkaEvent kafkaEvent = new MessageCreatedKafkaEvent(
        message.getId(),
        message.getChannel().getId(),
        message.getAuthor().getId(),
        message.getContent(),
        event.getAuthorName(),
        message.getChannel().getName(),
        message.getCreatedAt()
    );
    String payload = objectMapper.writeValueAsString(kafkaEvent);
    kafkaTemplate.send("discodeit.MessageCreatedEvent", payload);
    log.info("[Kafka] 메시지 생성 이벤트 전송 완료");
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) throws JsonProcessingException {
    RoleUpdatedKafkaEvent kafkaEvent = new RoleUpdatedKafkaEvent(
        event.getUser().getId(),
        event.getUser().getUsername(),
        event.getOldRole(),
        event.getNewRole()
    );
    String payload = objectMapper.writeValueAsString(kafkaEvent);
    kafkaTemplate.send("discodeit.RoleUpdatedEvent", payload);
    log.info("[Kafka] 권한 변경 이벤트 전송 완료");
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) throws JsonProcessingException {
    S3UploadFailedKafkaEvent kafkaEvent = new S3UploadFailedKafkaEvent(
        event.getBinaryContentId()
    );
    String payload = objectMapper.writeValueAsString(kafkaEvent);
    kafkaTemplate.send("discodeit.S3UploadFailedEvent", payload);
    log.info("[Kafka] S3 업로드 실패 이벤트 전송 완료");
  }
}
