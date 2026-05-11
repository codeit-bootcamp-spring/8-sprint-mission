package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(MessageCreatedEvent event) {
        MessageDto message = event.messageDto();
        String destination = String.format("/sub/channels.%s.messages", message.channelId());

        log.debug("[웹소켓 브로드캐스트] 채널 구독자에게 메시지 전송: destination={}", destination);
        messagingTemplate.convertAndSend(destination, message);
    }
}