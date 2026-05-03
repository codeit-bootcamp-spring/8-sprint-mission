package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Kafka 기반 NotificationRequiredTopicListener로 대체되어 비활성화
@Profile("disabled")
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final CacheManager cacheManager;

  @Async("taskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void on(MessageCreatedEvent event) {
    // 해당 채널 알림이 켜진 모든 사용자 조회
    List<ReadStatus> targets = readStatusRepository.findAllByChannel_IdAndNotificationEnabledTrue(event.channelId());

    List<Notification> notifications = targets.stream()
        .map(ReadStatus::getUser)
        .filter(user -> !user.getId().equals(event.authorId())) // 메시지 작성자 제외
        .map(receiver -> new Notification(
            receiver,
            String.format("%s (#%s)", event.authorName(), event.channelName()),
            event.content()
        ))
        .toList();

    notificationRepository.saveAll(notifications);

    // 수신자들의 캐시 무효화
    Cache cache = cacheManager.getCache("notificationsByUserId");
    if (cache != null) {
      targets.forEach(target -> cache.evict(target.getUser().getId()));
    }
  }

  @Async("taskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void on(RoleUpdatedEvent event) {
    User user = userRepository.findById(event.userId()).orElseThrow();
    Notification notification = new Notification(
        user,
        "권한이 변경되었습니다.",
        String.format("%s -> %s", event.oldRole(), event.newRole())
    );
    notificationRepository.save(notification);

    Cache roleCache = cacheManager.getCache("notificationsByUserId");
    if (roleCache != null) {
      roleCache.evict(event.userId());
    }
  }
}
