package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @Async("binaryContentTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBinaryContentCreated(BinaryContentCreatedEvent event) {
    log.debug("[BinaryContentEventListener] 바이너리 데이터 저장 시작 - ID: {}, 파일명: {}",
        event.binaryContentId(), event.fileName());

    try {
      binaryContentStorage.put(event.binaryContentId(), event.data());

      log.info("[BinaryContentEventListener] 바이너리 데이터 저장 완료 - ID: {}", event.binaryContentId());
    } catch (Exception e) {

      log.error("[BinaryContentEventListener] 바이너리 데이터 저장 실패 - ID: {}, 파일명: {}, 원인: {}",
          event.binaryContentId(), event.fileName(), e.getMessage());
    }
  }
}
