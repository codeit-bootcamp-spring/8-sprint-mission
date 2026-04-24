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
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(event);
    kafkaTemplate.send("discodeit.MessageCreatedEvent", payload);
    log.info("[Kafka] 메시지 생성 이벤트 전송 완료");
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(event);
    kafkaTemplate.send("discodeit.RoleUpdatedEvent", payload);
    log.info("[Kafka] 권한 변경 이벤트 전송 완료");
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) throws JsonProcessingException {
    String payload = objectMapper.writeValueAsString(event);
    kafkaTemplate.send("discodeit.S3UploadFailedEvent", payload);
    log.info("[Kafka] S3 업로드 실패 이벤트 전송 완료");
  }
}
