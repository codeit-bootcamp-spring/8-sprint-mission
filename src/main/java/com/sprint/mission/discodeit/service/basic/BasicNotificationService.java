package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.notification.NotificationForbiddenException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

  private final com.sprint.mission.discodeit.repository.NotificationRepository notificationRepository;
  private final com.sprint.mission.discodeit.mapper.NotificationMapper notificationMapper;

  @Transactional(readOnly = true)
  @Override
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    log.debug("알림 목록 조회 시작: receiverId={}", receiverId);

    return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId).stream()
        .map(notificationMapper::toDto)
        .toList();
  }

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
    log.info("알림 삭제 완료: id={}", notificationId);
  }

}
