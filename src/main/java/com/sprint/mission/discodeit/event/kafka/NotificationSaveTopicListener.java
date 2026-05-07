package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.enums.Role;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSaveTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationService notificationService;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent", groupId = "discodeit-db-group")
  public void saveMessageNotification(String kafkaEvent) throws Exception {
    MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

    List<ReadStatus> activeStatuses = readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(
        event.channelId());
    for (ReadStatus status : activeStatuses) {
      if (status.getUser().getId().equals(event.authorId())) {
        continue;
      }
      String title = event.authorName() + " (#" + event.channelName() + ")";
      // db 저장
      notificationService.create(status.getUser().getId(), title, event.content());
    }
  }

  @KafkaListener(topics = "discodeit.RoleUpdatedEvent", groupId = "discodeit-db-group")
  public void saveRoleNotification(String kafkaEvent) throws Exception {
    RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
    String title = "권한이 변경되었습니다.";
    String content = event.oldRole() + " -> " + event.newRole();
    // db 저장
    notificationService.create(event.userId(), title, content);
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent", groupId = "discodeit-db-group")
  public void saveS3FailedNotification(String kafkaEvent) throws Exception {
    S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
    List<User> admins = userRepository.findAllByRole(Role.ADMIN);
    String content = String.format("RequestId: %s\nContentId: %s\nError: %s", event.requestId(),
        event.binaryContentId(), event.errorMessage());
    for (User admin : admins) {
      // db 저장
      notificationService.create(admin.getId(), "[시스템 에러] 파일 업로드 실패", content);
    }
  }
}
