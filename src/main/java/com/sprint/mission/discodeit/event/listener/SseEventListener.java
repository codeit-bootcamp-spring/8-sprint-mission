package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.DomainEvent;
import com.sprint.mission.discodeit.service.basic.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Async("taskExecutor")
@Component
@RequiredArgsConstructor
@Slf4j
public class SseEventListener {

  private final SseService sseService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT,
      fallbackExecution = true)
  public void onSseEvent(DomainEvent<?> event) {
    log.info("[SSE 전송 준비] 이벤트명: {}, 데이터: {}, 대상자들: {}", event.eventName(), event.data(),
        event.targetUserIds());
    if (event.targetUserIds() == null || event.targetUserIds().isEmpty()) {
      sseService.broadcast(event.eventName(), event.data());
    } else {
      sseService.send(event.targetUserIds(), event.eventName(), event.data());
    }
  }
}
