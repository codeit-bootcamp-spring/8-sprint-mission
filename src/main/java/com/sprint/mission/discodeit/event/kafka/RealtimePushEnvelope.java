package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * WebSocket/SSE 실시간 푸시를 Kafka로 브로드캐스트하기 위한 페이로드.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RealtimePushEnvelope(
        String type,
        String destination,
        String body,
        String eventName,
        List<String> receiverIds,
        String eventId
) {
    public static final String TYPE_WS = "WS";
    public static final String TYPE_SSE_SEND = "SSE_SEND";
    public static final String TYPE_SSE_BROADCAST = "SSE_BROADCAST";
}
