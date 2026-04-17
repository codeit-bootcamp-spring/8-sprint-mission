package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

  void save(Notification notification);

  List<NotificationDto> findAll(UUID userId);

  void deleteByIdIfOwner(UUID userId, UUID notificationId);
}
