package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.service.SseService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(KafkaTemplate.class)
public class RealtimePushKafkaListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final SseService sseService;

    @KafkaListener(
            topics = RealtimePushEventPublisher.TOPIC,
            containerFactory = "realtimePushKafkaListenerContainerFactory"
    )
    public void onRealtimePush(String payload) {
        try {
            RealtimePushEnvelope envelope = objectMapper.readValue(payload, RealtimePushEnvelope.class);
            String t = envelope.type();
            if (RealtimePushEnvelope.TYPE_WS.equals(t)) {
                handleWebSocket(envelope);
            } else if (RealtimePushEnvelope.TYPE_SSE_SEND.equals(t)) {
                handleSseSend(envelope);
            } else if (RealtimePushEnvelope.TYPE_SSE_BROADCAST.equals(t)) {
                handleSseBroadcast(envelope);
            } else {
                log.warn("알 수 없는 실시간 푸시 타입: {}", t);
            }
        } catch (JsonProcessingException e) {
            log.error("실시간 푸시 Kafka 역직렬화 실패", e);
        } catch (Exception e) {
            log.error("실시간 푸시 처리 실패", e);
        }
    }

    private void handleWebSocket(RealtimePushEnvelope envelope) throws JsonProcessingException {
        MessageDto dto = objectMapper.readValue(envelope.body(), MessageDto.class);
        messagingTemplate.convertAndSend(envelope.destination(), dto);
        log.debug("WS 로컬 전달 완료, destination={}", envelope.destination());
    }

    private void handleSseSend(RealtimePushEnvelope envelope) {
        List<String> ids = envelope.receiverIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Set<UUID> receivers = new HashSet<>();
        for (String id : ids) {
            receivers.add(UUID.fromString(id));
        }
        sseService.deliverToLocalReceivers(receivers, envelope.eventName(), envelope.body(),
                UUID.fromString(envelope.eventId()));
    }

    private void handleSseBroadcast(RealtimePushEnvelope envelope) {
        sseService.deliverBroadcastToLocalEmitters(envelope.eventName(), envelope.body(),
                UUID.fromString(envelope.eventId()));
    }
}
