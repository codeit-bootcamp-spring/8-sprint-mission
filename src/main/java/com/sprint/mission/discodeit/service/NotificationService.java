package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

  //사용자 전체 알림 조회
  List<NotificationDto> findAllByReceiverId(UUID receiverId);

  // 알림 확인(삭제)
  void delete(UUID notificationId, UUID requesterId);

}
