package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.NotificationDto;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

  List<NotificationDto> findAll(UUID userId);

  void deleteByIdIfOwner(UUID userId, UUID notificationId);
}
