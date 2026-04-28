package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import com.sprint.mission.discodeit.service.SseService;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicSseService implements SseService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;

  @Override
  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    sseEmitterRepository.save(receiverId, emitter);

    // 연결 종료 시 Emitter 자동 삭제
    emitter.onCompletion(() -> sseEmitterRepository.delete(receiverId, emitter));
    emitter.onTimeout(() -> sseEmitterRepository.delete(receiverId, emitter));
    emitter.onError(e -> {
      log.error("SSE connection error for receiver: {}", receiverId, e);
      sseEmitterRepository.delete(receiverId, emitter);
    });

    // 1. 최초 연결 시 503 방지용 더미 이벤트 전송
    ping(emitter, "connected");

    // 2. 유실된 이벤트가 있다면 전송
    if (lastEventId != null) {
      List<SseMessage> missedMessages = sseMessageRepository.findAllAfter(lastEventId);
      for (SseMessage message : missedMessages) {
        sendToEmitter(emitter, message);
      }
    }

    return emitter;
  }

  @Override
  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    UUID eventId = UUID.randomUUID();
    SseMessage message = new SseMessage(eventId, eventName, data);
    sseMessageRepository.save(eventId, message);

    receiverIds.forEach(receiverId -> {
      List<SseEmitter> emitters = sseEmitterRepository.findAllByReceiverId(receiverId);
      emitters.forEach(emitter -> {
        boolean success = sendToEmitter(emitter, message);
        if (!success) {
          sseEmitterRepository.delete(receiverId, emitter);
        }
      });
    });
  }

  @Override
  public void broadcast(String eventName, Object data) {
    UUID eventId = UUID.randomUUID();
    SseMessage message = new SseMessage(eventId, eventName, data);
    sseMessageRepository.save(eventId, message);

    Map<UUID, List<SseEmitter>> allEmitters = sseEmitterRepository.findAll();
    allEmitters.forEach((receiverId, emitters) ->
        emitters.forEach(emitter -> {
          boolean success = sendToEmitter(emitter, message);
          if (!success) {
            sseEmitterRepository.delete(receiverId, emitter);
          }
        })
    );
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30) // 30분마다 실행
  public void cleanUp() {
    log.info("Running SSE cleanup task...");
    Map<UUID, List<SseEmitter>> allEmitters = sseEmitterRepository.findAll();
    allEmitters.forEach((receiverId, emitters) ->
        emitters.forEach(emitter -> {
          boolean success = ping(emitter, "keep-alive");
          if (!success) {
            log.warn("Deleting expired SSE emitter for receiver: {}", receiverId);
            sseEmitterRepository.delete(receiverId, emitter);
          }
        })
    );
  }

  private boolean ping(SseEmitter emitter, String data) {
    try {
      emitter.send(SseEmitter.event().name("ping").data(data));
      return true;
    } catch (IOException | IllegalStateException e) {
      return false;
    }
  }

  private boolean sendToEmitter(SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event()
          .id(message.id().toString())
          .name(message.name())
          .data(message.data()));
      return true;
    } catch (IOException | IllegalStateException e) {
      // 클라이언트 연결이 끊긴 경우
      return false;
    }
  }
}