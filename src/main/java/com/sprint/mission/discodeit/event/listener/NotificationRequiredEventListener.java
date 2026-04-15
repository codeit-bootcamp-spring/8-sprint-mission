package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationRepository notificationRepository;

  @Async("taskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {

    String authorName = event.author().getUsername();
    String channelName = event.channel().getName();
    String content = event.content();

    List<ReadStatus> targets = readStatusRepository.findAllByChannelId(event.channel().getId())
        .stream()
        .filter(ReadStatus::isNotificationEnabled)
        .filter(readStatus -> !readStatus.getUser().getId().equals(event.author().getId()))
        .toList();

    for (ReadStatus readStatus : targets) {
      Notification notification = new Notification(
          readStatus.getUser(),
          String.format("%s (#%s)", authorName, channelName),
          content
      );

      notificationRepository.save(notification);
    }
  }

  @Async("taskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    String title = "권한이 변경되었습니다.";
    String previousRole = event.previousRole();
    String newRole = event.newRole();

    Notification notification = new Notification(
        event.user(),
        title,
        String.format("%s -> %s", previousRole, newRole)
    );

    notificationRepository.save(notification);
  }
}
