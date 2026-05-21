package com.sprint.mission.discodeit.event.kafka;

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

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) {
    kafkaTemplate.send("discodeit.MessageCreatedEvent", event);

  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    kafkaTemplate.send("discodeit.RoleUpdatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(S3UploadFailedEvent event) {
    kafkaTemplate.send("discodeit.S3UploadFailedEvent", event);
  }
}

