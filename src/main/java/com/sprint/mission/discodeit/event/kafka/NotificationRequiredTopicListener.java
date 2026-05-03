package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationService notificationService;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  @Value("${discodeit.admin.username}")
  private String adminUsername;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);
      List<ReadStatus> readStatuses = readStatusRepository.findByChannelIdAndNotificationEnabledTrue(
          event.channelId());

      for (ReadStatus readStatus : readStatuses) {
        if (readStatus.getUser().getId().equals(event.authorId())) {
          continue;
        }
        String title = event.username() + " (# " + event.channelName() + ")";
        String content = event.content();
        notificationService.create(readStatus.getUser().getId(), title, content);
      }

    } catch (JsonProcessingException e) {
      log.error("kafka 알림 에러 - MessageCreatedEvent : {}", e.getMessage());
    }
  }

  @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
      String title = "권한이 변경되었습니다.";
      String content = event.oldRole() + " -> " + event.newRole();
      notificationService.create(event.userId(), title, content);
    } catch (JsonProcessingException e) {
      log.error("kafka 알림 에러 - RoleUpdatedEvent : {}", e.getMessage());
    }
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
      User admin = userRepository.findByUsername(adminUsername)
          .orElseThrow(() -> UserNotFoundException.withUsername(adminUsername));
      String title = "S3 파일 업로드 실패";
      StringBuffer sb = new StringBuffer();
      sb.append("RequestId: ").append(event.requestId()).append("\n");
      sb.append("BinaryContentId: ").append(event.binaryContentId()).append("\n");
      sb.append("Error: ").append(event.error()).append("\n");
      String content = sb.toString();
      notificationService.create(admin.getId(), title, content);

    } catch (JsonProcessingException e) {
      log.error("kafka 알림 에러 - S3UploadFailedEvent : {}", e.getMessage());
    }
  }
}

