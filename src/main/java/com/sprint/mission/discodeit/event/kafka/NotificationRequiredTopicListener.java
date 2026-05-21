package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnBean(KafkaTemplate.class)
public class NotificationRequiredTopicListener {

  private static final int TITLE_MAX_LEN = 500;

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);
      UUID authorId = event.authorId();
      String title = truncateTitle(event.authorUsername() + " (#" + event.channelLabel() + ")");
      String body = event.messageContent() != null ? event.messageContent() : "";
      for (ReadStatus readStatus : readStatusRepository.findAllByChannelIdWithUser(event.channelId())) {
        if (!readStatus.isNotificationEnabled()) {
          continue;
        }
        UUID receiverId = readStatus.getUser().getId();
        if (receiverId.equals(authorId)) {
          continue;
        }
        notificationService.createForReceiver(receiverId, title, body);
      }
      log.debug("Kafka MessageCreatedEvent 처리 완료, messageId={}", event.messageId());
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패 — 메시지를 skip합니다. topic={}, error={}",
          "discodeit.MessageCreatedEvent", e.getMessage());
    }
  }

  @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
      String content = event.previousRole() + " -> " + event.newRole();
      notificationService.createForReceiver(
          event.userId(),
          "권한이 변경되었습니다.",
          content);
      log.debug("Kafka RoleUpdatedEvent 처리 완료, userId={}", event.userId());
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패 — 메시지를 skip합니다. topic={}, error={}",
          "discodeit.RoleUpdatedEvent", e.getMessage());
    }
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
      String body = "작업: " + event.operationName() + "\n"
          + "RequestId: " + event.requestId() + "\n"
          + "BinaryContentId: " + event.binaryContentId() + "\n"
          + (event.byteLength() != null ? "Bytes: " + event.byteLength() + "\n" : "")
          + "Error: " + event.errorMessage();
      userRepository.findAllByRole(Role.ADMIN).forEach(admin ->
          notificationService.createForReceiver(
              admin.getId(),
              "S3 바이너리 저장 재시도 실패",
              body));
      log.debug("Kafka S3UploadFailedEvent 수신, binaryContentId={}", event.binaryContentId());
    } catch (JsonProcessingException e) {
      log.error("Kafka 메시지 역직렬화 실패 — 메시지를 skip합니다. topic={}, error={}",
          "discodeit.S3UploadFailedEvent", e.getMessage());
    }
  }

  private static String truncateTitle(String title) {
    if (title.length() <= TITLE_MAX_LEN) {
      return title;
    }
    return title.substring(0, TITLE_MAX_LEN);
  }
}
