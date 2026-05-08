package com.sprint.mission.discodeit.event.listener.sse;

import com.sprint.mission.discodeit.dto.dto.NotificationDto;
import com.sprint.mission.discodeit.event.Sse.NotificationCreatedEvent;
import com.sprint.mission.discodeit.service.Sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
//@Component
@RequiredArgsConstructor
public class NotificationSseDeliveryListener {

    private final SseService sseService;

    @Async("asyncTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(NotificationCreatedEvent event) {
        List<NotificationDto> notifications = event.getData();
        notifications.forEach(notification -> {
            UUID receiverId = notification.receiverId();
            sseService.send(Set.of(receiverId), "notification.created", notification);
        });
    }
}
