package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.kafka.MessageCreatedKafkaEvent;
import com.sprint.mission.discodeit.dto.kafka.RoleUpdatedKafkaEvent;
import com.sprint.mission.discodeit.dto.kafka.S3UploadFailedKafkaEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationService notificationService;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedKafkaEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedKafkaEvent.class);
      UUID channelId = event.channelId();
      UUID authorId = event.authorId();

      readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(channelId).stream()
          .map(rs -> rs.getUser().getId())
          .filter(receiverId -> !receiverId.equals(authorId))
          .forEach(receiverId -> {
            userRepository.findById(receiverId).ifPresent(receiver -> {
              notificationService.create(
                  receiver,
                  "새 메시지",
                  event.content()
              );
            });
          });

      log.info("[Kafka Consumer] 메시지 알림 처리 완료");
    } catch (JsonProcessingException e) {
      log.error("수신 데이터 변환 에러", e);
    }
  }

  @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedKafkaEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedKafkaEvent.class);
      userRepository.findById(event.userId()).ifPresent(user -> {
        String title = "권한이 변경되었습니다.";
        String content = String.format("%s -> %s", event.oldRole().name(), event.newRole().name());
        notificationService.create(user, title, content);
      });
      log.info("[Kafka Consumer] 권한 변경 알림 처리 완료");
    } catch (JsonProcessingException e) {
      log.error("수신 데이터 변환 에러", e);
    }
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedKafkaEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedKafkaEvent.class);
      log.error("[Kafka Consumer] S3 업로드 실패 이벤트 수신: binaryContentId={}",
          event.binaryContentId());
      // TODO: 실패 처리 로직 (예: 재시도, 관리자 알림 등)
    } catch (JsonProcessingException e) {
      log.error("수신 데이터 변환 에러", e);
    }
  }
}
