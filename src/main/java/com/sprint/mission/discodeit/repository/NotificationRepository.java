package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  @EntityGraph(attributePaths = "receiver")
  List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(UUID receiverId);

  @Query("SELECT n FROM Notification n JOIN FETCH n.receiver WHERE n.id = :notificationId")
  Optional<Notification> findByIdWithReceiver(@Param("notificationId") UUID notificationId);
}
