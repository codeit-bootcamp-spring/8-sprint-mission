package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.config.AsyncConfig;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.nio.file.Files;

@Slf4j
@RequiredArgsConstructor
@Component
public class BinaryContentEventListener {

    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentService binaryContentService;

    @Async(AsyncConfig.ASYNC_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBinaryContentCreatedEvent(BinaryContentCreatedEvent event) {
        log.info("바이너리 콘텐츠 저장 이벤트 수신: binaryContentId={}", event.binaryContentId());
        try {
            byte[] bytes = Files.readAllBytes(event.tempFile().toPath());
            binaryContentStorage.put(event.binaryContentId(), bytes);
            log.info("바이너리 콘텐츠 저장 완료: binaryContentId={}", event.binaryContentId());
            updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
        } catch (Exception e) {
            log.error("바이너리 콘텐츠 저장 실패: binaryContentId={}", event.binaryContentId(), e);
            updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
            throw new RuntimeException(e);
        } finally {
            if (event.tempFile() != null && event.tempFile().exists()) {
                boolean deleted = event.tempFile().delete();
                if (!deleted) {
                    log.warn("임시 파일 삭제 실패: {}", event.tempFile().getAbsolutePath());
                }
            }
        }
    }

    private void updateStatus(java.util.UUID binaryContentId, BinaryContentStatus status) {
        binaryContentService.updateStatus(binaryContentId, status);
    }
}
