package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  @Override
  @Cacheable(value = "notificationsByUserId", key = "#receiverId")
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    log.info("[CACHE_MISS] receiverId={} 의 알림 목록을 DB에서 조회합니다.", receiverId);
    return notificationRepository.findAllByReceiver_IdOrderByCreatedAtDesc(receiverId)
        .stream()
        .map(notificationMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  @CacheEvict(value = "notificationsByUserId", key = "#requesterId")
  public void delete(UUID notificationId, UUID requesterId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException(notificationId));

    // 본인의 알림만 확인 및 삭제 가능
    if (!notification.getReceiver().getId().equals(requesterId)) {
      throw new AccessDeniedException("알림을 삭제할 권한이 없습니다.");
    }

    notificationRepository.delete(notification);
  }
}
