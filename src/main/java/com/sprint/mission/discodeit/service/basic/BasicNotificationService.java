package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.notification.NotificationDeleteNotAllowedException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  /*
   * S3 업로드 실패(@Recover)와 같이 호출 측 트랜잭션이 이미 실패한 상태에서도
   * 알림을 독립적으로 저장해야 하는 경우를 위해 새 트랜잭션을 생성
   * 일반적인 성공 이벤트 리스너에서는 이 메서드 대신 Repository를 직접 호출하여
   * 리스너의 트랜잭션 범위 내에서 알림을 저장
   * */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  @CacheEvict(value = "notification", key = "#notification.receiver.id")
  public void send(Notification notification) {
    notificationRepository.save(notification);
  }

  @Cacheable(
      value = "notification", key = "#userId"
  )
  @Override
  public List<NotificationDto> findAll(UUID userId) {

    return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(notificationMapper::toDto)
        .toList();
  }

  @CacheEvict(value = "notification", key = "#userId")
  @Transactional
  @Override
  public void deleteByIdIfOwner(UUID userId, UUID notificationId) {

    Notification notification = notificationRepository.findByIdWithReceiver(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException(notificationId));

    UUID ownerId = notification.getReceiver().getId();

    if (!userId.equals(ownerId)) {
      throw new NotificationDeleteNotAllowedException();
    }

    notificationRepository.delete(notification);
  }
}
