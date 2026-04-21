package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final NotificationRepository notificationRepository;
  private final ReadStatusRepository readStatusRepository;

  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    Message message = event.getMessage();
    List<ReadStatus> readStatuses = readStatusRepository.findAllByChannelIdWithUser(
        message.getChannel().getId());

    List<Notification> notifications = readStatuses.stream()
        .filter(ReadStatus::isNotificationEnabled)
        .map(ReadStatus::getUser)
        .filter(user -> !user.getId().equals(message.getAuthor().getId()))
        .map(user -> {
          String channelName =
              message.getChannel().getName() != null ? message.getChannel().getName() : "";
          String title = String.format("%s (#%s)", message.getAuthor().getUsername(), channelName);
          String content = message.getContent();
          return new Notification(user, title, content);
        })
        .toList();

    notificationRepository.saveAll(notifications);
  }

  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    User user = event.getUser();
    String title = "권한이 변경되었습니다.";
    String content = String.format("%s -> %s", event.getOldRole().name(),
        event.getNewRole().name());

    Notification notification = new Notification(user, title, content);
    notificationRepository.save(notification);
  }
}
