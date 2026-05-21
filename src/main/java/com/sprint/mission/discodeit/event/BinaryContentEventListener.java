package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * BinaryContentEventListener - 실제 업로드 처리 담당
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;
  private final ApplicationEventPublisher eventPublisher;

  // 메타데이터를 저장한 트랜잭션이 최종적으로 DB에 반영된 후에만 실행된다.
  // BinaryContentCreatedEvent 타입의 이벤트가 발생되면 아래의 메서드가 실행된다.
  @Async("taskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBinaryContentCreated(BinaryContentCreatedEvent event) {
    log.info("[ASYNC_EVENT] 스토리지 저장 시작 binaryContentId={}", event.binaryContentId());

    try {

      // 실제 스토리지에 저장
      binaryContentStorage.put(event.binaryContentId(), event.bytes());

      // 성공 시 상태 업데이트
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);

      log.info("[이벤트 리스너] 스토리지 저장 성공 binaryContentId={}", event.binaryContentId());
      eventPublisher.publishEvent(
          new BinaryContentStatusUpdatedEvent(binaryContentService.findById(event.binaryContentId()))
      );

    } catch (Exception e) {
      log.error("[EVENT_LISTENER] 스토리지 저장 실패 binaryContentId={}", event.binaryContentId(), e);

      // 실패 시 상태 업데이트
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);

      // S3 업로드 실패 이벤트 발행 (알림 서비스로 전달)
      eventPublisher.publishEvent(new S3UploadFailedEvent(event.binaryContentId()));
      eventPublisher.publishEvent(
          new BinaryContentStatusUpdatedEvent(binaryContentService.findById(event.binaryContentId()))
      );
    }
  }
}
