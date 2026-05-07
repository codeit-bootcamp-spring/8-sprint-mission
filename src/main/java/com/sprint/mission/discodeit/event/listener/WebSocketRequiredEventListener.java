package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

  // Spring에서 제공하는 웹소켓 메시지 전송용 템플릿
  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMessage(MessageCreatedEvent event) {

    // 웹소켓으로 구독할 엔드포인트
    String destination = "/sub/channels." + event.channelId() + ".messages";

    // 생성된 메시지 이벤트를 해당 채널을 구독중인 모든 유저에게 전송
    messagingTemplate.convertAndSend(destination, event);
  }
}
