package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseService {

  private static final long DEFAULT_TIMEOUT = 60L * 60L * 1000L;
  private final SseEmitterRepository emitterRepository;
  private final SseMessageRepository messageRepository;

  public SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitterRepository.add(receiverId, emitter);
    emitter.onCompletion(() -> emitterRepository.remove(receiverId, emitter));
    emitter.onTimeout(() -> emitterRepository.remove(receiverId, emitter));
    ping(emitter);
    if (lastEventId != null) {
      Map<UUID, Object> missed = messageRepository.findAfter(lastEventId);
      missed.forEach((eventId, data) -> sendToEmitter(emitter, eventId, "replay", data));
    }
    return emitter;
  }

  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    UUID eventId = UUID.randomUUID();
    messageRepository.put(eventId, data);
    for (UUID receiverId : receiverIds) {
      for (SseEmitter emitter : new ArrayList<>(emitterRepository.findByReceiverId(receiverId))) {
        sendToEmitter(emitter, eventId, eventName, data);
      }
    }
  }

  public void broadcast(String eventName, Object data) {
    UUID eventId = UUID.randomUUID();
    messageRepository.put(eventId, data);
    for (var emitters : emitterRepository.findAll()) {
      for (SseEmitter emitter : new ArrayList<>(emitters)) {
        sendToEmitter(emitter, eventId, eventName, data);
      }
    }
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    for (var emitters : emitterRepository.findAll()) {
      for (SseEmitter emitter : new ArrayList<>(emitters)) {
        ping(emitter);
      }
    }
  }

  private boolean ping(SseEmitter emitter) {
    return sendToEmitter(emitter, UUID.randomUUID(), "ping", "ping");
  }

  private boolean sendToEmitter(SseEmitter emitter, UUID eventId, String eventName, Object data) {
    try {
      emitter.send(SseEmitter.event()
          .id(eventId.toString())
          .name(eventName)
          .data(data));
      return true;
    } catch (IOException | IllegalStateException e) {
      emitter.complete();
      return false;
    }
  }
}
