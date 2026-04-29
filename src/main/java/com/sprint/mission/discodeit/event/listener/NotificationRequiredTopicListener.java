package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.DomainEvent;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveFailedException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.notification.NotificationFailedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.SseService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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


  private final ReadStatusRepository readStatusRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;
  private final CacheManager cacheManager;
  private final NotificationMapper notificationMapper;
  private final ChannelRepository channelRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${admin.username}")
  private String adminUsername;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  @Transactional
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);
      log.info("[NotificationRequiredTopicListener] 메시지 생성 알림 처리 시작 - MessageId: {}",
          event.messageDto().id());

      UUID authorId = event.messageDto().author().id();
      UUID channelId = event.messageDto().channelId();
      String authorName = event.messageDto().author().username();
      Channel channel = channelRepository.findById(channelId)
          .orElseThrow(() -> new ChannelNotFoundException(channelId));
      String channelName = (channel.getName() == null) ? "비공개 채널" : channel.getName();
      String content = event.messageDto().content();

      List<ReadStatus> targets = readStatusRepository.findAllByChannelId(channelId)
          .stream()
          .filter(ReadStatus::isNotificationEnabled)
          .filter(readStatus -> !readStatus.getUser().getId().equals(authorId))
          .toList();

      for (ReadStatus readStatus : targets) {
        User receiver = readStatus.getUser();
        Notification notification = new Notification(
            receiver,
            String.format("%s (#%s)", authorName, channelName),
            content
        );
        notificationRepository.save(notification);

        eventPublisher.publishEvent(
            new DomainEvent<>("notifications.created", notificationMapper.toDto(notification),
                List.of(receiver.getId())));

        evictNotificationCache(receiver.getId());

        Cache channelCache = cacheManager.getCache("channel");
        if (channelCache != null) {
          channelCache.evict(receiver.getId());
        }
      }
      log.info("[NotificationRequiredTopicListener] 메시지 생성 알림 처리 완료 - 수신 대상: {}명", targets.size());

    } catch (JsonProcessingException e) {
      log.error("[NotificationRequiredTopicListener] 메시지 생성 이벤트 JSON 파싱 실패", e);
    } catch (Exception e) {
      log.error("[NotificationRequiredTopicListener] 메시지 생성 알림 처리 중 예외 발생", e);
      throw new NotificationFailedException(e);
    }
  }

  @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
  @Transactional
  public void onRoleUpdatedEvent(String kafkaEvent) {
    try {
      RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
      log.info("[NotificationRequiredTopicListener] 권한 변경 알림 처리 시작 - 대상자: {}", event.userName());

      User receiver = userRepository.findById(event.userId())
          .orElseThrow(() -> new UserNotFoundException(event.userId()));

      Notification notification = new Notification(
          receiver,
          "권한이 변경되었습니다.",
          String.format("%s -> %s", event.previousRole(), event.newRole())
      );

      notificationRepository.save(notification);

      eventPublisher.publishEvent(
          new DomainEvent<>("notifications.created", notificationMapper.toDto(notification),
              List.of(receiver.getId())));

      evictNotificationCache(event.userId());
      log.info("[NotificationRequiredTopicListener] 권한 변경 알림 처리 완료 - 대상자: {}", event.userName());
    } catch (JsonProcessingException e) {
      log.error("[NotificationRequiredTopicListener] 권한 변경 이벤트 JSON 파싱 실패", e);
    } catch (Exception e) {
      log.error("[NotificationRequiredTopicListener] 권한 변경 알림 처리 중 예외 발생", e);
      throw new NotificationFailedException(e);
    }
  }

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
  @Transactional
  public void onS3UploadFailedEvent(String kafkaEvent) {
    try {
      S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
      log.info(
          "[NotificationRequiredTopicListener] S3 업로드 실패 알림 처리 시작 - BinaryContentId: {}, RequestId: {}",
          event.binaryContentId(), event.requestId());

      User admin = userRepository.findByUsername(adminUsername)
          .orElseThrow(() -> new UserNotFoundException(adminUsername));

      String title = "S3 파일 업로드 실패";

      Notification notification = new Notification(
          admin,
          title,
          String.format("RequestId: %s\n BinaryContentId: %s\n Error: %s",
              event.requestId(),
              event.binaryContentId(),
              event.errorMessage())
      );

      notificationRepository.save(notification);

      eventPublisher.publishEvent(
          new DomainEvent<>("notifications.created", notificationMapper.toDto(notification),
              List.of(admin.getId())));

      evictNotificationCache(admin.getId());

      log.info("[NotificationRequiredTopicListener] S3 업로드 실패 알림 처리 완료 - 관리자: {}",
          admin.getUsername());
    } catch (JsonProcessingException e) {
      log.error("[NotificationRequiredTopicListener] S3 업로드 실패 이벤트 JSON 파싱 실패", e);
    } catch (Exception e) {
      log.error("[NotificationRequiredTopicListener] S3 업로드 실패 알림 처리 중 예외 발생", e);
      throw new BinaryContentSaveFailedException(e);
    }
  }

  private void evictNotificationCache(UUID userId) {
    Cache cache = cacheManager.getCache("notification");

    if (cache != null) {
      cache.evict(userId);
      log.debug("[NotificationRequiredTopicListener] 사용자 알림 캐시 삭제 완료 - UserId: {}", userId);
    }
  }
}
