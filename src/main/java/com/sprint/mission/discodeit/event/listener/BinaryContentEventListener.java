package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.enums.BinaryContentStatus;
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

  // 실제 파일 저장하는 컴포넌트
  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBinaryContentCreatedEvent(BinaryContentCreatedEvent event) {

    try {
      log.info("파일 업로드 이벤트 수신: 시작 (id={})", event.binaryContentId());

      // 업로드 저장 작업 수행
      binaryContentStorage.put(event.binaryContentId(), event.bytes());

      // 성공 시, 상태 업데이트
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
      log.info("파일 업로드 이벤트 수신: 성공");
    } catch (RuntimeException e) {
      log.error("파일 스토리지 업로드 최종 실패 (id={})", event.binaryContentId(), e);

      // 실패 시, 상태 업데이트
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);

      // 비동기 스레드 풀의 에러 핸들러로 예외를 전파하여 의도를 명확히 함
      throw e;
    }
  }
}
