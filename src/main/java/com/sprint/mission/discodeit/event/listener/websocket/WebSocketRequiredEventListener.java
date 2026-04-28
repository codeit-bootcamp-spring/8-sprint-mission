package com.sprint.mission.discodeit.event.listener.websocket;

import com.sprint.mission.discodeit.dto.dto.MessageDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(MessageCreatedEvent event) {

        MessageDto message = event.getData();
        UUID channelId = message.channelId();

        // 전송할 엔드포인트 생성
        String destination = "/sub/channels." + channelId + ".messages";

        // 구독자들에게 메시지 전송
        messagingTemplate.convertAndSend(destination, message);
    }
}
