package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.BinaryContent;
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
  public void onBinaryContentCreated(BinaryContentCreatedEvent event) {
    BinaryContent binaryContent = event.getBinaryContent();
    UUID id = binaryContent.getId();

    log.info("바이너리 업로드 시작: id={}", id);

    try {
      binaryContentStorage.put(id, event.getBytes());

      binaryContentService.updateStatus(id, BinaryContentStatus.SUCCESS);
      log.info("바이너리 업로드 성공 및 상태 변경 완료: id={}", id);

    } catch (Exception e) {
      log.error("바이너리 업로드 중 오류 발생: id={}", id, e);
      binaryContentService.updateStatus(id, BinaryContentStatus.FAIL);
    }
  }
}
