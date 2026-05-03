package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.SseBroadcastMessage;
import com.sprint.mission.discodeit.service.basic.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseEventListener {

  private final SseService sseService;
  private final ObjectMapper objectMapper;

  @KafkaListener(
      topics = "discodeit.SseBroadcast",
      groupId = "sse-group-#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void onSseEvent(String kafkaEvent) {
    try {
      SseBroadcastMessage message = objectMapper.readValue(kafkaEvent, SseBroadcastMessage.class);
      log.info("[SseEventListener] 이벤트명: {}, 대상자 수: {}", message.eventName(),
          message.targetUserIds() != null ? message.targetUserIds().size() : "전체");

      if (message.targetUserIds() == null || message.targetUserIds().isEmpty()) {
        sseService.broadcast(message.eventName(), message.data());
      } else {
        sseService.send(message.targetUserIds(), message.eventName(), message.data());
      }
    } catch (JsonProcessingException e) {
      log.error("[SseEventListener] JSON 파싱 에러 - Payload: {}", kafkaEvent, e);
    } catch (Exception e) {
      log.error("[SseEventListener] 알 수 없는 전송 에러 발생", e);
    }
  }
}
