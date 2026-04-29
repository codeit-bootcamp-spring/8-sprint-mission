package com.sprint.mission.discodeit.event.listener.sse;

import com.sprint.mission.discodeit.event.Sse.User.UserCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserDeletedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserUpdatedEvent;
import com.sprint.mission.discodeit.service.Sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSseDeliveryListener {

    private final SseService sseService;

    @Async("userTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserCreatedEvent event) {
        UUID userId = event.userDto().id();
        try {
            sseService.broadcast("users.created", event.userDto());
            log.debug("SSE 유저 생성 이벤트 전송 성공 : userId: {}", userId);
        } catch (Exception e) {
            log.error("SSE 유저 생성 이벤트 전송 실패: userId: {}, error: {}", userId, e.getMessage());
        }
    }

    @Async("userTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserUpdatedEvent event) {
        UUID userId = event.userDto().id();
        try {
            sseService.broadcast("users.updated", event.userDto());
            log.debug("SSE 유저 수정 이벤트 전송 성공 : userId: {}", userId);
        } catch (Exception e) {
            log.error("SSE 유저 수정 이벤트 전송 실패: userId: {}, error: {}", userId, e.getMessage());
        }
    }

    @Async("userTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserDeletedEvent event) {
        UUID userId = event.userDto().id();
        try {
            sseService.broadcast("users.updated", event.userDto());
            log.debug("SSE 유저 삭제 이벤트 전송 성공 : userId: {}", userId);
        } catch (Exception e) {
            log.error("SSE 유저 삭제 이벤트 전송 실패: userId: {}, error: {}", userId, e.getMessage());
        }
    }
}
