package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

    private final NotificationRepository notificationRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(MessageCreatedEvent event) {
        log.info("[NotificationListener] MessageCreatedEvent 수신 - ChannelId: {}", event.channelId());

        List<ReadStatus> targetReadStatuses = readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(event.channelId());

        List<Notification> notifications = targetReadStatuses.stream()
                .map(ReadStatus::getUser)
                .filter(user -> !user.getId().equals(event.senderId())) // 본인 제외
                .map(receiver -> {
                    return new Notification(
                            receiver,
                            String.format("%s (#%s)", event.senderName(), event.channelName()),
                            event.content()
                    );
                })
                .toList();

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
            log.info("[NotificationListener] {}건의 메시지 알림 생성 완료", notifications.size());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(RoleUpdatedEvent event) {
        log.info("[NotificationListener] RoleUpdatedEvent 수신 - UserId: {}", event.userId());

        // userId를 가진 프록시 객체를 생성함
        User proxyUser = userRepository.getReferenceById(event.userId());

        Notification notification = new Notification(
                proxyUser,
                "권한이 변경되었습니다.",
                String.format("%s -> %s", event.oldRole(), event.newRole())
        );

        notificationRepository.save(notification);
        log.info("[NotificationListener] 권한 변경 알림 생성 완료 - UserId: {}", event.userId());
    }
}
