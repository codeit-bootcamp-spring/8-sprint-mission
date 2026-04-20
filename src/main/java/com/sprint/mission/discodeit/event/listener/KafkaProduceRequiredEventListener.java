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
@Component
@RequiredArgsConstructor
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("taskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    try {
      log.info("[KafkaProduceRequiredEventListener] 메시지 생성 이벤트 발행 시도: {}", event.messageId());
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("discodeit.MessageCreatedEvent", payload);
    } catch (JsonProcessingException e) {
      log.error("[KafkaProduceRequiredEventListener] 직렬화 실패 - Event: {}", event, e);
    } catch (Exception e) {
      log.error("[KafkaProduceRequiredEventListener] Kafka 전송 중 예외 발생 - Event: {}", event, e);
    }
  }

  @Async("taskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    try {
      log.info("[KafkaProduceRequiredEventListener] 권한 변경 이벤트 발행 시도: {}", event.userId());
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("discodeit.RoleUpdatedEvent", payload);
    } catch (JsonProcessingException e) {
      log.error("[KafkaProduceRequiredEventListener] 직렬화 실패 - Event: {}", event, e);
    } catch (Exception e) {
      log.error("[KafkaProduceRequiredEventListener] Kafka 전송 중 예외 발생 - Event: {}", event, e);
    }
  }

  @Async("taskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) throws JsonProcessingException {
    try {
      log.info("[KafkaProduceRequiredEventListener] 바이너리 데이터 실패 이벤트 발행 시도: {}",
          event.binaryContentId());
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("discodeit.S3UploadFailedEvent", payload);
    } catch (JsonProcessingException e) {
      log.error("[KafkaProduceRequiredEventListener] 직렬화 실패 - Event: {}", event, e);
    } catch (Exception e) {
      log.error("[KafkaProduceRequiredEventListener] Kafka 전송 중 예외 발생 - Event: {}", event, e);
    }
  }
}
