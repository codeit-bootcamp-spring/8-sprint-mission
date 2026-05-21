package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.SseMessage;
import com.sprint.mission.discodeit.event.SseKafkaEvent;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import com.sprint.mission.discodeit.service.SseService;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicSseService implements SseService {

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final Long TIMEOUT = 1000L * 60 * 60;
  private static final String SSE_TOPIC = "sse-events";

  @Override
  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter sseEmitter = new SseEmitter(TIMEOUT);
    sseEmitterRepository.save(receiverId, sseEmitter);

    sseEmitter.onCompletion(() -> {
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });

    sseEmitter.onTimeout(() -> {
      sseEmitterRepository.delete(receiverId, sseEmitter);
      sseEmitter.complete();
    });

    sseEmitter.onError(e -> {
      sseEmitterRepository.delete(receiverId, sseEmitter);
    });

    if (!ping(sseEmitter)) {
      sseEmitterRepository.delete(receiverId, sseEmitter);
      return sseEmitter;
    }

    if (lastEventId != null) {
      List<SseMessage> lostMessages = sseMessageRepository.findAllAfter(lastEventId);

      lostMessages.forEach(message -> {
        try {
          sseEmitter.send(SseEmitter.event()
              .id(message.id().toString())
              .name(message.eventName())
              .data(message.data()));
        } catch (IOException e) {
          log.error("유실 이벤트 복원 실패: eventId={}", message.id());
        }
      });
    }

    return sseEmitter;
  }

  @Override
  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    UUID eventId = sseMessageRepository.save(eventName, data);

    SseKafkaEvent kafkaEvent = new SseKafkaEvent(eventId, receiverIds, eventName, data);
    kafkaTemplate.send(SSE_TOPIC, kafkaEvent);
  }

  @Override
  public void broadcast(String eventName, Object data) {
    UUID eventId = sseMessageRepository.save(eventName, data);

    SseKafkaEvent kafkaEvent = new SseKafkaEvent(eventId, null, eventName, data);
    kafkaTemplate.send(SSE_TOPIC, kafkaEvent);
  }

  @Override
  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    Collection<UUID> receiverIds = sseEmitterRepository.findAllReceiverIds();

    for (UUID receiverId : receiverIds) {
      List<SseEmitter> emitters = sseEmitterRepository.findAllReceiverId(receiverId);

      for (SseEmitter sseEmitter : emitters) {
        if (!ping(sseEmitter)) {
          sseEmitterRepository.delete(receiverId, sseEmitter);
        }
      }
    }
    sseMessageRepository.clean(Instant.now().minusSeconds(3600));
  }

  @KafkaListener(
      topics = SSE_TOPIC,
      groupId = "#{T(java.util.UUID).randomUUID().toString()}"
  )
  public void consume(SseKafkaEvent event) {
    if (event.receiverIds() == null) {
      List<SseEmitter> emitters = sseEmitterRepository.findAll();
      emitters.forEach(emitter -> sendEvent(emitter, event));
    } else {
      event.receiverIds().forEach(receiverId -> {
        List<SseEmitter> emitters = sseEmitterRepository.findAllReceiverId(receiverId);
        emitters.forEach(emitter -> sendEvent(emitter, event));
      });
    }

  }

  private boolean ping(SseEmitter sseEmitter) {
    try {
      sseEmitter.send(SseEmitter.event()
          .name("ping")
          .data(""));
      return true;
    } catch (IOException e) {
      log.error("ping 실패 : {}", e.getMessage());
      return false;
    }
  }

  private void sendEvent(SseEmitter emitter, SseKafkaEvent event) {
    try {
      emitter.send(SseEmitter.event()
          .id(event.eventId().toString())
          .name(event.eventName())
          .data(event.data()));
    } catch (IOException e) {
      log.error("SSE 전송 실패: eventId={}, error={}", event.eventId(), e.getMessage());
      emitter.completeWithError(e);
    }
  }
}
