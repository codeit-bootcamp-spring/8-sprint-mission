package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;

public interface NotificationService {

  List<NotificationDto> findAllForCurrentUser(Authentication authentication);

  void delete(UUID notificationId, Authentication authentication);

  void createForReceiver(UUID receiverUserId, String title, String content);
}
