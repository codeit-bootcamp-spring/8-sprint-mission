package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.service.MessageService;
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
    private final MessageService messageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(MessageCreatedEvent event) {
        MessageDto messageDto = messageService.findMessage(event.messageId());
        messagingTemplate.convertAndSend(
            "/sub/channels." + event.channelId() + ".messages",
            messageDto
        );
        log.info("[WEBSOCKET] 메시지 브로드캐스트 channelId={}", event.channelId());
    }
}
