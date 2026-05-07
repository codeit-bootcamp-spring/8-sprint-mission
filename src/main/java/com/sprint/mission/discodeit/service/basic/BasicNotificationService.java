package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.enums.AuthErrorCode;
import com.sprint.mission.discodeit.exception.enums.NotificationErrorCode;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.SseService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final SseService sseService;

  @Override
  @CacheEvict(value = "notifications", key = "#receiverId")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void create(UUID receiverId, String title, String content) {

    Notification notification = new Notification(receiverId, title, content);
    notificationRepository.save(notification);
    log.info("알림 생성 완료 (receiverId: {}", receiverId);

    // SSE를 통해서 클라이언트에 알림 이벤트 전송
    NotificationDto notificationDto = notificationMapper.toNotificationDto(notification);
    sseService.send(Collections.singletonList(receiverId), "notification.created", notificationDto);
  }

  @Override
  @Cacheable(value = "notifications", key = "#receiverId")
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {

    return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId)
        .stream()
        .map(notificationMapper::toNotificationDto)
        .toList();
  }

  @Override
  @CacheEvict(value = "notifications", key = "#requesterId")
  @Transactional
  public void deleteById(UUID notificationId, UUID requesterId) {

    // 알림 존재 확인
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new DiscodeitException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

    // 권한 확인
    if (!notification.getReceiverId().equals(requesterId)) {
      throw new DiscodeitException(AuthErrorCode.ACCESS_DENIED, "본인의 알림만 삭제할 수 있습니다.");
    }

    notificationRepository.delete(notification);
    log.info("알림 삭제 완료. notificationId: {}", notificationId);
  }
}
