package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.service.SseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseRequiredEventListener {

    private final SseService sseService;

    // Kafka consumer 트랜잭션 컨텍스트에서도 안정적으로 동작하도록 @EventListener 사용
    // NotificationDto가 이벤트에 포함되어 있어 DB 조회 없이 클라이언트에서 바로 표시 가능
    @EventListener
    public void on(NotificationCreatedEvent event) {
        sseService.send(List.of(event.receiverId()), "notifications.created", event.notification());
    }

    @EventListener
    public void on(UserPresenceChangedEvent event) {
        sseService.broadcast("users.updated", event.userDto());
    }

    // BinaryContent 상태 변경은 비동기 컨텍스트에서 발행 → @EventListener 사용
    @EventListener
    public void on(BinaryContentStatusUpdatedEvent event) {
        sseService.broadcast("binaryContents.updated", event.binaryContent());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelCreatedEvent event) {
        sseService.broadcast("channels.created", event.channel());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelUpdatedEvent event) {
        sseService.broadcast("channels.updated", event.channel());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelDeletedEvent event) {
        sseService.broadcast("channels.deleted", event.channel());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserCreatedEvent event) {
        sseService.broadcast("users.created", event.user());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserUpdatedEvent event) {
        sseService.broadcast("users.updated", event.user());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserDeletedEvent event) {
        sseService.broadcast("users.deleted", event.user());
    }
}
