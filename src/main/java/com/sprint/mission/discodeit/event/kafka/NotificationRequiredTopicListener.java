package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationRepository notificationRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final MessageRepository messageRepository;
  private final CacheManager cacheManager;
  private final NotificationMapper notificationMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

      List<ReadStatus> targets = readStatusRepository.findAllByChannel_IdAndNotificationEnabledTrue(
          event.channelId());

      List<Notification> notifications = targets.stream()
          .map(ReadStatus::getUser)
          .filter(user -> !user.getId().equals(event.authorId()))
          .map(receiver -> new Notification(
              receiver,
              String.format("%s (#%s)", event.authorName(), event.channelName()),
              event.content()
          ))
          .toList();

      notificationRepository.saveAll(notifications);

      Cache cache = cacheManager.getCache("notificationsByUserId");
      if (cache != null) {
        targets.forEach(target -> cache.evict(target.getUser().getId()));
      }

      notifications.forEach(notification ->
          eventPublisher.publishEvent(new NotificationCreatedEvent(
              notification.getReceiver().getId(),
              notificationMapper.toDto(notification)
          ))
      );

      log.info("[KAFKA_CONSUMER] MessageCreatedEvent 처리 완료 channelId={}, 알림 수={}", event.channelId(), notifications.size());
    } catch (JsonProcessingException e) {
      // 역직렬화 실패는 재시도해도 의미 없으므로 예외를 전파하지 않고 offset을 커밋한다.
      log.error("[KAFKA_CONSUMER] MessageCreatedEvent 역직렬화 실패 - 메시지 건너뜀", e);
    }
  }

  @Transactional
  @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);

      User user = userRepository.findById(event.userId()).orElseThrow();
      Notification notification = new Notification(
          user,
          "권한이 변경되었습니다.",
          String.format("%s -> %s", event.oldRole(), event.newRole())
      );
      notificationRepository.save(notification);

      Cache cache = cacheManager.getCache("notificationsByUserId");
      if (cache != null) {
        cache.evict(event.userId());
      }

      eventPublisher.publishEvent(new NotificationCreatedEvent(
          user.getId(),
          notificationMapper.toDto(notification)
      ));

      log.info("[KAFKA_CONSUMER] RoleUpdatedEvent 처리 완료 userId={}", event.userId());
    } catch (JsonProcessingException e) {
      log.error("[KAFKA_CONSUMER] RoleUpdatedEvent 역직렬화 실패 - 메시지 건너뜀", e);
    }
  }

  @Transactional
  @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);

      // 프로필 이미지 업로드 실패한 경우 사용자 조회
      Optional<User> maybeUser = userRepository.findByProfile_Id(event.binaryContentId());

      // 프로필이 아닌 경우, 메시지 첨부파일로 업로드한 사용자 조회
      if (maybeUser.isEmpty()) {
        maybeUser = messageRepository.findFirstByAttachments_Id(event.binaryContentId())
            .map(Message::getAuthor);
      }

      if (maybeUser.isEmpty()) {
        log.warn("[KAFKA_CONSUMER] S3UploadFailedEvent: 업로드 실패한 파일의 소유자를 찾을 수 없음 binaryContentId={}", event.binaryContentId());
        return;
      }

      User receiver = maybeUser.get();
      Notification notification = new Notification(
          receiver,
          "파일 업로드에 실패했습니다.",
          String.format("binaryContentId: %s", event.binaryContentId())
      );
      notificationRepository.save(notification);

      Cache cache = cacheManager.getCache("notificationsByUserId");
      if (cache != null) {
        cache.evict(receiver.getId());
      }

      log.info("[KAFKA_CONSUMER] S3UploadFailedEvent 처리 완료 userId={}", receiver.getId());
    } catch (JsonProcessingException e) {
      log.error("[KAFKA_CONSUMER] S3UploadFailedEvent 역직렬화 실패 - 메시지 건너뜀", e);
    }
  }
}
