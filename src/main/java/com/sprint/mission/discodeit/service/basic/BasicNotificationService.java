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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  @Override
  public List<NotificationDto> findAll(UUID userId) {

    return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(notificationMapper::toDto)
        .toList();
  }

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
