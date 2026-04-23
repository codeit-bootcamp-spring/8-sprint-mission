package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  // 특정 사용자의 알림을 최신순으로 조회
  List<Notification> findAllByReceiver_IdOrderByCreatedAtDesc(UUID receiverId);
}
