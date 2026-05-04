package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationForbiddenException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

  private final com.sprint.mission.discodeit.repository.NotificationRepository notificationRepository;
  private final com.sprint.mission.discodeit.mapper.NotificationMapper notificationMapper;

  @Cacheable(value = "notifications", key = "#receiverId")
  @Transactional(readOnly = true)
  @Override
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {

    log.info("[Cache Miss] DB에서 알림 목록을 가져옵니다: userId={}", receiverId);

    return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId).stream()
        .map(notificationMapper::toDto)
        .toList();
  }

  @CacheEvict(value = "notifications", key = "#requesterId")
  @Transactional
  @Override
  public void delete(UUID notificationId, UUID requesterId) {
    log.debug("알림 삭제 시작: id={}, requesterId={}", notificationId, requesterId);

    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));

    if (!notification.getReceiver().getId().equals(requesterId)) {
      log.warn("알림 삭제 권한 없음: notificationId={}, requesterId={}", notificationId, requesterId);
      throw new NotificationForbiddenException(); // 403 Forbidden
    }

    notificationRepository.delete(notification);
    log.info("[Cache Evict] 알림 삭제로 인해 캐시를 비웁니다.");
  }

  @CacheEvict(value = "notifications", key = "#receiver.id")
  @Transactional
  @Override
  public void create(User receiver, String title, String content) {
    Notification notification = new Notification(receiver, title, content);
    notificationRepository.save(notification);
    log.info("[Cache Evict] 새 알림 생성으로 인해 userId={} 의 캐시를 비웁니다.", receiver.getId());
  }

}
