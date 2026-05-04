package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.dto.NotificationDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface NotificationService {

    List<NotificationDto> findAllByReceiver(UUID receiverId);

    void confirmAndDelete(UUID notificationId, UUID userId);

    void create(Set<UUID> receiverIds, String title, String content);
}
