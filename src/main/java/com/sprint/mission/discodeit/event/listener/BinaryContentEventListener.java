package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.base.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentSaveFailedException;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.basic.SseService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final SseService sseService;

  @Async("taskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBinaryContentCreated(BinaryContentCreatedEvent event) {
    log.info("[BinaryContentEventListener] 바이너리 데이터 저장 시작 - ID: {}, 파일명: {}",
        event.binaryContentDto().id(), event.binaryContentDto().fileName());

    try {
      binaryContentStorage.put(event.binaryContentDto().id(), event.data());
      binaryContentService.updateStatus(event.binaryContentDto().id(), BinaryContentStatus.SUCCESS);

      try {
        switch (event.binaryContentType()) {
          case PROFILE_IMAGE:
            sseService.broadcast("binaryContents.updated", event.binaryContentDto());
            break;
          case MESSAGE_FILE:
            if (ChannelType.PRIVATE.equals(event.channelType())) {
              if (event.participantIds() != null && !event.participantIds().isEmpty()) {
                sseService.send(event.participantIds(), "binaryContents.updated",
                    event.binaryContentDto());
              }
            } else {
              sseService.broadcast("binaryContents.updated", event.binaryContentDto());
            }
        }
      } catch (Exception e) {
        log.warn("[BinaryContentEventListener] 실시간 알림 전송 실패 - 사유: {}", e.getMessage());
      }

      log.info("[BinaryContentEventListener] 바이너리 데이터 저장 완료 - ID: {}",
          event.binaryContentDto().id());
    } catch (Exception e) {
      binaryContentService.updateStatus(event.binaryContentDto().id(), BinaryContentStatus.FAIL);

      try {
        switch (event.binaryContentType()) {
          case PROFILE_IMAGE:
            if (event.userId() != null) {
              sseService.send(List.of(event.userId()), "binaryContents.updated",
                  event.binaryContentDto());
            }
            break;
          case MESSAGE_FILE:
            if (ChannelType.PRIVATE.equals(event.channelType())) {
              if (event.participantIds() != null && !event.participantIds().isEmpty()) {
                sseService.send(event.participantIds(), "binaryContents.updated",
                    event.binaryContentDto());
              }
            } else {
              sseService.broadcast("binaryContents.updated", event.binaryContentDto());
            }
        }
      } catch (Exception sseEx) {
        log.warn("[BinaryContentEventListener] 실패 알림 발송 중 추가 오류 발생: {}", sseEx.getMessage());
      }

      log.error("[BinaryContentEventListener] 바이너리 데이터 저장 실패 - ID: {}, 파일명: {}, 원인: {}",
          event.binaryContentDto().id(), event.binaryContentDto().fileName(), e.getMessage());
      throw new BinaryContentSaveFailedException(e);
    }
  }
}
