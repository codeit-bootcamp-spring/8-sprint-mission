package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.event.kafka.RealtimePushEventPublisher;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

    private final ObjectProvider<RealtimePushEventPublisher> realtimePushEventPublisher;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(MessageCreatedEvent event) {
        MessageDto dto = messageService.find(event.messageId());
        String destination = "/sub/channels." + event.channelId() + ".messages";
        RealtimePushEventPublisher publisher = realtimePushEventPublisher.getIfAvailable();
        if (publisher != null) {
            try {
                publisher.publishWebSocket(destination, dto);
                log.debug("웹소켓 메시지 Kafka 발행 완료, destination={}, messageId={}", destination,
                        event.messageId());
            } catch (JsonProcessingException e) {
                log.error("웹소켓 Kafka 페이로드 직렬화 실패, messageId={}", event.messageId(), e);
                messagingTemplate.convertAndSend(destination, dto);
            }
        } else {
            messagingTemplate.convertAndSend(destination, dto);
            log.debug("웹소켓 메시지 로컬 브로드캐스트 완료, destination={}, messageId={}", destination,
                    event.messageId());
        }
    }
}
