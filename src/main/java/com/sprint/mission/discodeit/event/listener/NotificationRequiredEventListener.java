package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.config.AsyncConfig;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private static final int TITLE_MAX_LEN = 500;

  private final ReadStatusRepository readStatusRepository;
  private final NotificationService notificationService;

  @Async(AsyncConfig.ASYNC_EXECUTOR)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) {
    UUID authorId = event.authorId();
    String title = truncateTitle(event.authorUsername() + " (#" + event.channelLabel() + ")");
    String body = event.messageContent() != null ? event.messageContent() : "";
    for (ReadStatus readStatus : readStatusRepository.findAllByChannelIdWithUser(event.channelId())) {
      if (!readStatus.isNotificationEnabled()) {
        continue;
      }
      UUID receiverId = readStatus.getUser().getId();
      if (receiverId.equals(authorId)) {
        continue;
      }
      notificationService.createForReceiver(receiverId, title, body);
      log.debug(
          "메시지 알림 생성: receiverId={}, messageId={}, channelId={}",
          receiverId,
          event.messageId(),
          event.channelId());
    }
  }

  @Async(AsyncConfig.ASYNC_EXECUTOR)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    String content = event.previousRole() + " -> " + event.newRole();
    notificationService.createForReceiver(
        event.userId(),
        "권한이 변경되었습니다.",
        content);
    log.debug(
        "권한 변경 알림 생성: userId={}, {} -> {}",
        event.userId(),
        event.previousRole(),
        event.newRole());
  }

  private static String truncateTitle(String title) {
    if (title.length() <= TITLE_MAX_LEN) {
      return title;
    }
    return title.substring(0, TITLE_MAX_LEN);
  }
}
