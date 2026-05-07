package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.enums.Role;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.SseService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPushTopicListener {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;
  private final SseService sseService;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  // 새 메시지 발생 시 알림 전송
  // 그룹 ID를 매번 다르게 생성해서, 서버 3대가 전부 메시지를 인터셉트할수 있도록함
  @KafkaListener(topics = "discodeit.MessageCreatedEvent", groupId = "discodeit-push-group-#{T(java.util.UUID).randomUUID().toString()}")
  public void pushMessageEvent(String kafkaEvent) throws Exception {
    MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

    // 웹소켓 (채팅방에 있는 사람들에게 실시간 전송)
    String destination = "/sub/channels." + event.channelId() + ".messages";
    messagingTemplate.convertAndSend(destination, event);

    // SSE (채팅방 밖에 있는 사람들에게 알림 팝업 전송)
    List<ReadStatus> activeStatuses = readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(
        event.channelId());

    for (ReadStatus status : activeStatuses) {
      if (status.getUser().getId().equals(event.authorId())) {
        continue;
      }

      String title = event.authorName() + " (#" + event.channelName() + ")";

      NotificationDto notificationDto = new NotificationDto(
          null,
          Instant.now(),
          status.getUser().getId(),
          title,
          event.content()
      );

      // 특정 유저 1명한테만 이벤트 발송
      sseService.send(Collections.singletonList(status.getUser().getId()), "notifications.created",
          notificationDto);
    }
    log.info("WebSocket/SSE broadcast 완료");
  }

  // 권한 변경 시 SSE 알림 전송
  @KafkaListener(topics = "discodeit.RoleUpdatedEvent", groupId = "discodeit-push-group-#{T(java.util.UUID).randomUUID().toString()}")
  public void pushRoleEvent(String kafkaEvent) throws Exception {
    RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);

    NotificationDto notificationDto = new NotificationDto(
        null,
        Instant.now(),
        event.userId(),
        "권한이 변경되었습니다.",
        event.oldRole() + " -> " + event.newRole()
    );

    sseService.send(Collections.singletonList(event.userId()), "notifications.created",
        notificationDto);
  }

  // S3 업로드 실패 시
  @KafkaListener(topics = "discodeit.S3UploadFailedEvent", groupId = "discodeit-push-group-#{T(java.util.UUID).randomUUID().toString()}")
  public void pushS3FailedEvent(String kafkaEvent) throws Exception {
    S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
    List<User> admins = userRepository.findAllByRole(Role.ADMIN);

    String content = String.format("RequestId: %s\nContentId: %s\nError: %s", event.requestId(),
        event.binaryContentId(), event.errorMessage());

    for (User admin : admins) {
      NotificationDto notificationDto = new NotificationDto(
          null,
          Instant.now(),
          admin.getId(),
          "[시스템 에러] 파일 업로드 실패",
          content
      );
      sseService.send(Collections.singletonList(admin.getId()), "notifications.created",
          notificationDto);
    }
  }
}
