package com.sprint.mission.discodeit.event.listener.sse;

import com.sprint.mission.discodeit.event.Sse.BinaryContent.BinaryContentUpdatedEvent;
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
public class BinaryContentSseDeliveryListener {

    private final SseService sseService;

    @Async("binaryContentTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BinaryContentUpdatedEvent event) {
        UUID binaryContentId = event.binaryContentDto().id();
        try {
            sseService.broadcast("binaryContents.updated", event.binaryContentDto());
            log.debug("SSE 파일 상태 변경 알림 전송: binaryContentId: {}, status: {}", binaryContentId, event.status());
        } catch (Exception e) {
            log.error("SSE 파일 상태 변경 알림 전송 실패: binaryContentId: {}, error: {}", binaryContentId, e.getMessage());
        }
    }
}
