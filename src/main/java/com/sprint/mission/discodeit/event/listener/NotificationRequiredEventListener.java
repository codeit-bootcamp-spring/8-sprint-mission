package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// @Component event Listener 기반
@RequiredArgsConstructor
@Slf4j
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;
  private final CacheManager cacheManager;

  @Async("taskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) {
    log.info("[NotificationRequiredEventListener] 메시지 생성 이벤트 수신 - MessageId: {}, Channel: {}",
        event.messageId(), event.channelName());

    try {
      UUID authorId = event.authorId();
      String authorName = event.authorName();
      UUID channelId = event.channelId();
      String channelName = (event.channelName() == null) ? "비공개 채널" : event.channelName();
      String content = event.content();

      List<ReadStatus> targets = readStatusRepository.findAllByChannelId(channelId)
          .stream()
          .filter(ReadStatus::isNotificationEnabled)
          .filter(readStatus -> !readStatus.getUser().getId().equals(authorId))
          .toList();

      log.debug("[NotificationRequiredEventListener] 알림 발송 대상자 수: {}명 (작성자: {})",
          targets.size(), event.authorName());

      for (ReadStatus readStatus : targets) {
        User receiver = readStatus.getUser();
        Notification notification = new Notification(
            receiver,
            String.format("%s (#%s)", authorName, channelName),
            content
        );

        notificationRepository.save(notification);
        evictNotificationCache(receiver.getId());
        log.debug("[NotificationRequiredEventListener] 알림 저장 완료 - 수신자: {}",
            readStatus.getUser().getUsername());
      }
      log.info("[NotificationRequiredEventListener] 메시지 알림 처리 완료 - 수신 대상: {}명", targets.size());
    } catch (Exception e) {
      log.error("[NotificationRequiredEventListener] 메시지 생성 이벤트 알림 처리 중 오류 발생 - Event: {}", event,
          e);
      throw new RuntimeException(e);
    }
  }

  @Async("taskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    log.info("[NotificationRequiredEventListener] 권한 변경 이벤트 수신 - 대상자: {}, 변경: {} -> {}",
        event.userName(), event.previousRole(), event.newRole());

    try {
      String title = "권한이 변경되었습니다.";
      String previousRole = event.previousRole();
      String newRole = event.newRole();

      User receiver = userRepository.findById(event.userId())
          .orElseThrow(() -> new UserNotFoundException(event.userId()));

      Notification notification = new Notification(
          receiver,
          title,
          String.format("%s -> %s", previousRole, newRole)
      );

      notificationRepository.save(notification);
      evictNotificationCache(receiver.getId());
      log.info("[NotificationRequiredEventListener] 권한 변경 알림 저장 완료 - 대상자: {}", event.userName());
    } catch (UserNotFoundException e) {
      log.warn("[NotificationRequiredEventListener] 알림 실패 - 존재하지 않는 사용자 ID: {}",
          event.userId());
    } catch (Exception e) {
      log.error("[NotificationRequiredEventListener] 권한 변경 이벤트 알림 처리 중 오류 발생 - Event: {}", event,
          e);
      throw new RuntimeException(e);
    }
  }

  private void evictNotificationCache(UUID userId) {
    Cache cache = cacheManager.getCache("notification");

    if (cache != null) {
      cache.evict(userId);
      log.debug("[NotificationRequiredEventListener] 사용자 알림 캐시 삭제 완료 - UserId: {}", userId);
    }
  }
}
