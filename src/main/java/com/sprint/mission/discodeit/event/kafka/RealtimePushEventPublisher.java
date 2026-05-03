package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(KafkaTemplate.class)
public class RealtimePushEventPublisher {

    public static final String TOPIC = "discodeit.RealtimePushEvent";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishWebSocket(String destination, MessageDto dto) throws JsonProcessingException {
        RealtimePushEnvelope envelope = new RealtimePushEnvelope(
                RealtimePushEnvelope.TYPE_WS,
                destination,
                objectMapper.writeValueAsString(dto),
                null,
                null,
                null
        );
        send(envelope);
    }

    public void publishSseTargeted(Collection<UUID> receiverIds, String eventName, String dataJson, UUID eventId)
            throws JsonProcessingException {
        List<String> ids = receiverIds.stream().map(UUID::toString).toList();
        RealtimePushEnvelope envelope = new RealtimePushEnvelope(
                RealtimePushEnvelope.TYPE_SSE_SEND,
                null,
                dataJson,
                eventName,
                ids,
                eventId.toString()
        );
        send(envelope);
    }

    public void publishSseBroadcast(String eventName, String dataJson, UUID eventId) throws JsonProcessingException {
        RealtimePushEnvelope envelope = new RealtimePushEnvelope(
                RealtimePushEnvelope.TYPE_SSE_BROADCAST,
                null,
                dataJson,
                eventName,
                List.of(),
                eventId.toString()
        );
        send(envelope);
    }

    private void send(RealtimePushEnvelope envelope) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(envelope);
        kafkaTemplate.send(TOPIC, json);
        log.debug("실시간 푸시 Kafka 발행, type={}, topic={}", envelope.type(), TOPIC);
    }
}
