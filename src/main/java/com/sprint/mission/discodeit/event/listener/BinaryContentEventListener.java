package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
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

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBinaryContentCreatedEvent(BinaryContentCreatedEvent event) {
    UUID binaryContentId = event.binaryContentId();
    try {
      binaryContentStorage.put(binaryContentId, event.bytes());
      binaryContentService.updateStatus(binaryContentId, BinaryContentStatus.SUCCESS);
    } catch (Exception e) {

      binaryContentService.updateStatus(binaryContentId, BinaryContentStatus.FAIL);
      log.warn("파일 업로드 실패: id = {}, message = {} ", binaryContentId, e.getMessage());
    }
  }
}
