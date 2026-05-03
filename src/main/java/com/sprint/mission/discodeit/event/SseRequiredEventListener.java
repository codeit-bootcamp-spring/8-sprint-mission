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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(NotificationCreatedEvent event) {
        sseService.send(List.of(event.receiverId()), "notifications.created", event.notification());
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
