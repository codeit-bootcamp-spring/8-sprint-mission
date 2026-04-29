package com.sprint.mission.discodeit.event.listener.sse;

import com.sprint.mission.discodeit.event.Sse.NotificationCreatedEvent;
import com.sprint.mission.discodeit.service.Sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSseDeliveryListener {

    private final SseService sseService;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(NotificationCreatedEvent event) {
        UUID receiverId = event.notificationDto().receiverId();
        // SSE 기본 전송(구독자)
        try {
            sseService.send(Set.of(receiverId), "notifications.created", event.notificationDto());
            log.debug("SSE 알림 생성 이벤트 전송 성공: notificationId: {}", receiverId);
        } catch (Exception e) {
            log.error("SSE 알림 생성 이벤트 전송 실패 : notificationId: {}", receiverId);
        }
    }
}
