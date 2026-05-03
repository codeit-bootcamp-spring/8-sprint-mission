package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.base.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.SseBroadcastMessage;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveFailedException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;
  private final ApplicationEventPublisher eventPublisher;

  @Async("taskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBinaryContentCreated(BinaryContentCreatedEvent event) {
    log.info("[BinaryContentEventListener] 바이너리 데이터 저장 시작 - ID: {}, 파일명: {}",
        event.binaryContentDto().id(), event.binaryContentDto().fileName());

    try {
      binaryContentStorage.put(event.binaryContentDto().id(), event.data());
      binaryContentService.updateStatus(event.binaryContentDto().id(), BinaryContentStatus.SUCCESS);

      publishUpdateEvent(event);

      log.info("[BinaryContentEventListener] 바이너리 데이터 저장 완료 - ID: {}",
          event.binaryContentDto().id());
    } catch (Exception e) {
      binaryContentService.updateStatus(event.binaryContentDto().id(), BinaryContentStatus.FAIL);

      publishUpdateEvent(event);

      log.error("[BinaryContentEventListener] 바이너리 데이터 저장 실패 - ID: {}, 파일명: {}, 원인: {}",
          event.binaryContentDto().id(), event.binaryContentDto().fileName(), e.getMessage());
      throw new BinaryContentSaveFailedException(e);
    }
  }

  private void publishUpdateEvent(BinaryContentCreatedEvent event) {
    try {
      SseBroadcastMessage broadcastMessage = null;
      switch (event.binaryContentType()) {
        case PROFILE_IMAGE:
          // 성공/실패 상관없이 브로드캐스트

          broadcastMessage = new SseBroadcastMessage("binaryContents.updated",
              event.binaryContentDto(), null);
          break;

        case MESSAGE_FILE:
          if (ChannelType.PRIVATE.equals(event.channelType()) && event.participantIds() != null) {
            // 비공개 채널은 참여자들에게만
            broadcastMessage = new SseBroadcastMessage("binaryContents.updated",
                event.binaryContentDto(),
                event.participantIds());
          } else {
            // 공개 채널은 브로드캐스트
            broadcastMessage = new SseBroadcastMessage("binaryContents.updated",
                event.binaryContentDto(),
                null);
          }
          break;
      }

      if (broadcastMessage != null) {
        eventPublisher.publishEvent(broadcastMessage);
      }
    } catch (Exception e) {
      log.warn("[BinaryContentEventListener] 실시간 알림 전송 중 오류 발생: {}", e.getMessage());
    }
  }
}
