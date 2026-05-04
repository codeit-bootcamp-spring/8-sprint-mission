package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.BinaryContentDeletedEvent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentService binaryContentService;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void handleCreatedEvent(BinaryContentCreatedEvent event) {
        log.info("[BinaryContentEventListener] - 파일 저장 이벤트를 진행중...");
        UUID id = event.binaryContentId();

        try {
            binaryContentStorage.put(id, event.bytes());
            binaryContentService.updateStatus(id, BinaryContentStatus.SUCCESS);
        } catch (Exception e) {
            binaryContentService.updateStatus(id, BinaryContentStatus.FAIL);
            log.error("[BinaryContentEventListener] - 파일 저장 이벤트 수행 중 에러 발생: ID: {}", id, e);
        }
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void handleDeletedEvent(BinaryContentDeletedEvent event) {
        log.info("[BinaryContentEventListener] - 파일 삭제 이벤트를 진행중...");
        UUID id = event.binaryContentId();

        try {
            binaryContentStorage.delete(id);
        } catch (Exception e) {
            log.error("[BinaryContentEventListener] - 파일 삭제 이벤트 수행 중 에러 발생: ID: {}", id, e);
        }
    }
}
