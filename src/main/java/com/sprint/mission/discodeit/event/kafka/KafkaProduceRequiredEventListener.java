package com.sprint.mission.discodeit.event.kafka;

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
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) throws JsonProcessingException {

    try {
      // 이벤트 객체 JSON 문자열 변환
      String payload = objectMapper.writeValueAsString(event);
      // 토픽 전송
      kafkaTemplate.send("discodeit.MessageCreatedEvent", payload);

      log.info("Kafka 발행 성공. Topic={}, MessageId={}", "discodeit.MessageCreatedEvent",
          event.messageId());
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패. JSON 변환 에러: {}", e.getMessage());
    }
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {

    try {

      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("discodeit.RoleUpdatedEvent", payload);

      log.info("Kafka 발행 성공: Topic={}, UserId={}", "discodeit.RoleUpdatedEvent", event.userId());
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패. JSON 변환 에러: {}", e.getMessage());
    }
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send("discodeit.S3UploadFailedEvent", payload);
      log.info("Kafka 발행 성공: S3 업로드 실패 알림");
    } catch (JsonProcessingException e) {
      log.error("Kafka 발행 실패. JSON 변환 에러: {}", e.getMessage());
    }
  }
}
