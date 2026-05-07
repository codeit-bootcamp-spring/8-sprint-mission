package com.sprint.mission.discodeit.domain.message.event.listener;

import com.sprint.mission.discodeit.domain.message.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@ConditionalOnProperty(prefix = "discodeit.notification", name = "listener", havingValue = "event", matchIfMissing = true)
@RequiredArgsConstructor
@Component
public class WebSocketRequiredEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(MessageCreatedEvent event) {
        String destination = String.format("/sub/channels.%s.messages", event.channelId());

        messagingTemplate.convertAndSend(destination, event.data());
    }
}
